# UniZip Explorer Context Menu Preview (Windows)

Version: Community 0.3.1 development (no stable release yet)

## Ownership

The shell integration stays in Community `FileAssociationService` and reuses the existing `MainApp` shell switches / `ArchiveService`. UniZip Pro imports the Community runtime; no duplicate `UniZipShell.exe` or native DLL is required for this classic menu preview.

## Install or remove without changing defaults

In the UniZip desktop app, open **Settings > System** and choose **Install Explorer context menu** or **Remove Explorer context menu**. These actions modify only `HKCU\Software\Classes` for the current Windows user; they do not set `.zip` default ProgID, delete Explorer UserChoice, or require Administrator privilege.

Menu verbs:
- Right-click `.zip`: UniZip > Open, Extract here, Extract into folder, Test archive, Generate SHA-256, SHA-512 or CRC-32 sidecar.
- Right-click file: UniZip > Add to ZIP or Generate SHA-256/SHA-512/CRC-32 sidecar. Right-click directory: UniZip > Add to ZIP (generates a new ZIP in the same directory, choosing a new name if it exists).

The `.zip` archive submenu is registered under `SystemFileAssociations\.zip` so it remains visible even if 7-Zip or another tool is the default application. Input ZIP creation menus use `*\shell` and `Directory\shell`. When right-clicking an existing ZIP you may therefore see both an archive menu and a compression menu in this **static V1**. For a single combined context-aware menu and reliable multi-select, a separately tested Explorer shell handler will be required; do not claim it is supported by this registry-only version.

## Supported shell launchers

- In `jpackage` packaged applications the registry command points to the **actual running EXE** using `jpackage.app-path`; therefore a Pro package launches `UniZip Pro.exe` and does not bypass Pro startup.
- In developer JAR/classpath runs the existing `javaw -jar` / `javaw -cp` fallback remains. The menu should be installed while running a built distributable; a developer classpath registration can become stale.
- Quotes protect spaces in executable paths and file paths; `%1` means a **single Explorer selection**.

## Safety/limits

- Only `.zip` is advertised for archive extraction/test. 7z/RAR/etc are **not** claimed to work.
- Extraction uses existing `ArchiveService.extractZip`, rename-on-conflict policy, and `SafeExtractTool` limits.
- ZIP creation calls existing `ArchiveService.createZip` and avoids replacing an existing output file.
- No prompts for overwriting existing output files from quick actions in this preview.
- Hash output uses the shared Community ChecksumTool (SHA-256, SHA-512, CRC-32). It writes a UTF-8 sha256sum-style .sha256 sidecar with CREATE_NEW and chooses a numbered filename rather than overwriting an existing sidecar.
- Multi-file selection, native Windows 11 primary context menu integration, and right-click background of a folder are **not** yet part of this slice. MD5 and checksum verification are not implemented. CRC-32 is an accidental-corruption check, not a secure authenticity proof. In Windows 11, the classic menu may require **Show more options**.
- If the portable executable is moved after registration, reinstall the context menu from the new path; removing the menu deletes only UniZip-owned verb keys.

## V1.1 protection and repair

- Every static verb, including its parent submenu, registers `MultiSelectModel=Single` so Windows does not start multiple destructive jobs for a multi-file selection. True multi-select belongs in Explorer V2 (issue #8).
- The **Install / Repair** button stays available after installation; use it when moving the portable folder to refresh the EXE command path. Remove is offered only when all owned menu entries are present.
- Quick ZIP creation stages the entire ZIP as a temporary file before final destination publish and does not request destination replacement. Existing archives are not intentionally overwritten; ZIP output inside its own source directory is rejected.
- Creating ZIP from a source directory containing symbolic links now fails closed and removes the staged output instead of including external linked content.
- Windows CI runs JUnit tests for existing-file preservation, Unicode/space file names, staging cleanup, invalid output location, and symbolic links.

## Background Explorer operations

- Explorer quick extract, ZIP creation, testing and SHA-256 run through one `ExplorerShellCommandService`, which delegates to existing Community `ArchiveService`/`ChecksumTool`. No duplicate ZIP or hashing engine was added.
- A non-modal, indeterminate progress dialog appears while `SwingWorker` performs disk I/O off the Swing event dispatch thread, keeping Windows UI responsive during larger operations.
- The progress window is **not** a cancellation control. It stays open until completion because abruptly cancelling a ZIP write without core rollback/cancellation semantics is unsafe. A later release can add explicit safe cancel with staging/cleanup tests.
- CLI accepts only the documented five shell commands and exactly one file path; unsupported verbs are not silently treated as successful operations.
- ZIP output naming retains dots in directory names and chooses numbered non-overwriting names for collisions.

## Explorer V2 hash groundwork

The classic one-selection Explorer menu now supports SHA-512 and CRC-32 alongside SHA-256.
Each action creates a non-overwriting sidecar and delegates to the same shared ChecksumTool in Community.
Hash results use the file bytes; CRC-32 is not a cryptographic signature. The modern Windows 11 menu
still requires a signed native IExplorerCommand implementation plus package identity/sparse package;
the static registry menu does **not** claim to implement it. Multiple selection and safe cancellation
remain separate gated changes.

## Avoid duplicate UniZip menus

Windows supports a static menu condition through Advanced Query Syntax. The generic
`*\\shell\\UniZip.Compress` now has `AppliesTo filters out .zip/.sha256/.sha512/.crc32`:
a ZIP gets the archive-only UniZip flyout (open/extract/test/hash), while other
files get create ZIP / checksum actions. Folder compression remains unchanged.

Associating file extensions no longer writes a second legacy UniZip context menu.
If the user's saved preference enables right-click menus, it installs/repairs the
same current-user Explorer verbs. The old UniZip-only ProgID/wildcard keys are cleaned
as part of migration; other vendors' associations are not modified by menu registration.
Windows 10/11 GUI visibility still needs manual acceptance.

## One-file checksum verification (V1.2 preview)

- Right-click a generated `.sha256`, `.sha512` or `.crc32` checksum file:
  **UniZip > Verify checksum**. Classic menu uses the existing Pro/Community EXE
  and shared `--verify-checksum` shell command, including the supported
  noninteractive `--batch` entrypoint. A mismatch returns a failed result,
  not a misleading PASS.
- Each checksum file stores one lowercase hexadecimal hash followed by
  exactly two spaces and the original filename in UTF-8. Only that same-folder
  basename is accepted; absolute paths, traversal, symlinks, multiline/large
  manifests and invalid hex are rejected before accessing the target.
- This verifies that bytes match the chosen checksum file, **not** that a file
  came from a trusted publisher. CRC-32 is not cryptographically secure.
- The generic compression flyout excludes .zip and checksum files, so one
  appropriate UniZip menu appears per supported selection. Multi-select is
  intentionally not supported until the native handler has safe IPC.
- The true Windows 11 primary menu, MSI install/upgrade, and GUI right-click
  visual acceptance remain separate release gates.

## Acceptance tests

1. On Windows 10 and 11, keep 7-Zip as default ZIP program; installing UniZip verbs must not change it.
2. Test a ZIP in folder with Turkish letters, quotes/spaces, nested folders; open, extract-here, extract-to-folder and test.
3. Right-click normal file and directory; create a ZIP; run again, verify collision gives non-overwriting numbered name. Create SHA-256 on an existing file twice, verifying the original sidecar is preserved.
4. Confirm user-level uninstall removes menu, without changing file associations or other vendors' verbs.
5. In a **Pro self-contained Windows ZIP**, menu commands must point to and launch `UniZip Pro.exe` (not Community-only Java launcher).
6. Verify Windows 11 **Show more options** behavior; native Windows 11 modern context menu remains a separate milestone.

Source: Microsoft documentation for SystemFileAssociations verbs and static cascading submenus:
- https://learn.microsoft.com/en-us/windows/win32/shell/app-registration
- https://learn.microsoft.com/en-us/windows/win32/shell/how-to--create-cascading-menus-with-the-subcommands-registry-entry
