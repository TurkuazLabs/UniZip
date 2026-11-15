# Dosya Yolu: REPO_ROOT/scripts/build_turkuaz_installer_pilot.ps1
# Amac: Self-contained UniZip app-image'i sabit TurkuazInstaller Community surumuyle pilot Setup EXE'ye paketlemek
# Modul - PowerShell Tool
# Version: 0.1.2
# Aciklama: Imzasiz pilot paketi atomik uretir ve gecici hedefte kurulum/kaldirma smoke testi yapar
# Bagimli Oldugu Katman: Tool | Config

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$InstallerCheckout,

    [Parameter(Mandatory = $true)]
    [string]$PayloadDirectory,

    [Parameter(Mandatory = $true)]
    [string]$OutputDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$installerRevision = '5454874e64ed2d78622755032b12c0d32b17b244'
$installerVersion = '0.2.22'
$applicationVersion = '0.3.1'
$packageId = 'unizip'
$entryPoint = 'UniZip.exe'

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Description,

        [Parameter(Mandatory = $true)]
        [scriptblock]$Action
    )

    & $Action
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

function Assert-OrdinaryTree {
    param([Parameter(Mandatory = $true)][string]$Path)

    $rootItem = Get-Item -LiteralPath $Path -Force -ErrorAction Stop
    if (($rootItem.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0) {
        throw "Reparse point refused: $Path"
    }

    foreach ($item in Get-ChildItem -LiteralPath $Path -Recurse -Force) {
        if (($item.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0) {
            throw "Reparse point refused: $($item.FullName)"
        }
    }
}

$repoRoot = (Resolve-Path -LiteralPath (Split-Path -Parent $PSScriptRoot)).Path
$installerRoot = (Resolve-Path -LiteralPath $InstallerCheckout).Path
$payloadRoot = (Resolve-Path -LiteralPath $PayloadDirectory).Path
$outputRoot = [System.IO.Path]::GetFullPath($OutputDirectory)
$resolvedApplicationVersion = (Get-Content -LiteralPath (Join-Path $repoRoot 'version.txt') -Raw).Trim()

if ($resolvedApplicationVersion -ne $applicationVersion) {
    throw "UniZip version must be $applicationVersion for this pilot; found $resolvedApplicationVersion."
}
if (Test-Path -LiteralPath $outputRoot) {
    throw "Output directory already exists; refusing to overwrite: $outputRoot"
}
Assert-OrdinaryTree -Path $payloadRoot
$payloadEntryPoint = Join-Path $payloadRoot $entryPoint
if (-not (Test-Path -LiteralPath $payloadEntryPoint -PathType Leaf)) {
    throw "UniZip payload entry point is missing: $payloadEntryPoint"
}

$actualRevision = (& git -C $installerRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $actualRevision -ne $installerRevision) {
    throw "TurkuazInstaller checkout is not the reviewed pilot revision $installerRevision."
}

$installerManifest = Join-Path $installerRoot 'rust\Cargo.toml'
if (-not (Test-Path -LiteralPath $installerManifest -PathType Leaf)) {
    throw 'TurkuazInstaller Rust workspace manifest is missing.'
}

$workRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("unizip-turkuaz-pilot-" + [guid]::NewGuid().ToString('N'))
$cargoTarget = Join-Path $workRoot 'cargo-target'
$stage = Join-Path $workRoot 'stage'
$smokeTarget = Join-Path $workRoot 'installed'
[void][System.IO.Directory]::CreateDirectory($stage)

try {
    Invoke-Checked -Description 'TurkuazInstaller CLI build' -Action {
        cargo build --locked --manifest-path $installerManifest `
            -p turkuaz-installer-cli --release --bin turkuaz-installer `
            --target-dir $cargoTarget
    }
    $builtInstaller = Join-Path $cargoTarget 'release\turkuaz-installer.exe'
    $cli = Join-Path $workRoot 'turkuaz-installer-cli.exe'
    Copy-Item -LiteralPath $builtInstaller -Destination $cli
    $reportedVersion = (& $cli --version).Trim()
    if ($LASTEXITCODE -ne 0 -or $reportedVersion -ne "Turkuaz Installer v$installerVersion") {
        throw "Unexpected TurkuazInstaller version: $reportedVersion"
    }

    Invoke-Checked -Description 'TurkuazInstaller Studio stub build' -Action {
        cargo build --locked --manifest-path $installerManifest `
            -p turkuaz-installer-cli --release --bin turkuaz-installer `
            --features setup-authoring-gui --target-dir $cargoTarget
    }
    $studioStub = Join-Path $workRoot 'turkuaz-setup-studio-stub.exe'
    Copy-Item -LiteralPath $builtInstaller -Destination $studioStub

    $packageName = "UniZip-v$applicationVersion.tzpkg"
    $setupName = "UniZip-TurkuazSetup-v$applicationVersion-PILOT.exe"
    $packagePath = Join-Path $stage $packageName
    $setupPath = Join-Path $stage $setupName

    Invoke-Checked -Description 'UniZip package build' -Action {
        & $cli build-package --package-id $packageId --version $applicationVersion `
            --source $payloadRoot --output $packagePath --executable $entryPoint
    }
    Invoke-Checked -Description 'UniZip native setup build' -Action {
        & $cli build-native-setup --stub $studioStub --package $packagePath --output $setupPath
    }

    $installProcess = Start-Process -FilePath $setupPath `
        -ArgumentList @('install-local-self', '--target', ('"' + $smokeTarget + '"')) `
        -WindowStyle Hidden -Wait -PassThru
    if ($installProcess.ExitCode -ne 0) {
        throw "UniZip pilot install smoke failed with exit code $($installProcess.ExitCode)."
    }
    $installedEntryPoint = Join-Path $smokeTarget $entryPoint
    if (-not (Test-Path -LiteralPath $installedEntryPoint -PathType Leaf)) {
        throw 'Pilot setup did not install the UniZip executable.'
    }
    $payloadHash = (Get-FileHash -LiteralPath $payloadEntryPoint -Algorithm SHA256).Hash
    $installedHash = (Get-FileHash -LiteralPath $installedEntryPoint -Algorithm SHA256).Hash
    if ($payloadHash -cne $installedHash) {
        throw 'Installed UniZip executable does not match the packaged payload.'
    }
    Invoke-Checked -Description 'UniZip pilot uninstall smoke' -Action {
        & $cli uninstall-local --package-id $packageId --target $smokeTarget --confirm-uninstall YES
    }
    if (Test-Path -LiteralPath $installedEntryPoint) {
        throw 'Pilot uninstall left the managed UniZip executable behind.'
    }

    $setupHash = (Get-FileHash -LiteralPath $setupPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $packageHash = (Get-FileHash -LiteralPath $packagePath -Algorithm SHA256).Hash.ToLowerInvariant()
    [System.IO.File]::WriteAllText(
        "$setupPath.sha256",
        "$setupHash  $setupName`n",
        [System.Text.Encoding]::ASCII
    )
    [System.IO.File]::WriteAllText(
        "$packagePath.sha256",
        "$packageHash  $packageName`n",
        [System.Text.Encoding]::ASCII
    )

    $metadata = [ordered]@{
        product = 'UniZip'
        application_version = $applicationVersion
        package_id = $packageId
        installer_version = $installerVersion
        installer_revision = $installerRevision
        java_runtime_bundled = $true
        setup_file = $setupName
        setup_sha256 = $setupHash
        package_file = $packageName
        package_sha256 = $packageHash
        install_uninstall_smoke = $true
        code_signed = $false
        production_release = $false
    }
    $metadata | ConvertTo-Json | Set-Content `
        -LiteralPath (Join-Path $stage 'UniZip-TurkuazSetup-PILOT.json') `
        -Encoding utf8
    @'
UniZip TurkuazInstaller Community pilot artifact
NOT A PRODUCTION RELEASE.
The setup is unsigned and intended only for controlled Windows pilot testing.
The existing UniZip portable release path remains authoritative.
'@ | Set-Content -LiteralPath (Join-Path $stage 'PILOT-NOTICE.txt') -Encoding utf8

    [void][System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($outputRoot))
    Move-Item -LiteralPath $stage -Destination $outputRoot
    Write-Host "UniZip TurkuazInstaller pilot verified: $outputRoot"
}
finally {
    if (Test-Path -LiteralPath $workRoot) {
        Remove-Item -LiteralPath $workRoot -Recurse -Force
    }
}
