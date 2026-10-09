# UniZip Explorer Context Menu Preview (Windows)

Version: Community 0.3.1 development (no stable release yet)

## Ownership

The shell integration stays in Community `FileAssociationService` and reuses the existing `MainApp` shell switches / `ArchiveService`. UniZip Pro imports the Community runtime; no duplicate `UniZipShell.exe` or native DLL is required for this classic menu preview.

## Install or remove without changing defaults

In the UniZip desktop app, open **Settings > System** and choose **Install Explorer context menu** or **Remove Explorer context menu**. These actions modify only `HKCU\Software\Classes` for the current Windows user; they do not set `.zip` default ProgID, delete Explorer UserChoice, or require Administrator privilege.

Menu verbs:
- Right-click `.zip`: UniZip > Open, Extract here, Extract into folder, Test archive, Generate SHA-256 sidecar.
- Right-click file: UniZip > Add to ZIP or Generate SHA-256 sidecar. Right-click directory: UniZip > Add to ZIP (generates a new ZIP in the same directory, choosing a new name if it exists).

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
- SHA-256 output uses the already-existing Community ChecksumTool. It writes a UTF-8 sha256sum-style .sha256 sidecar with CREATE_NEW and chooses a numbered filename rather than overwriting an existing sidecar.
- Multi-file selection, native Windows 11 primary context menu integration, and right-click background of a folder are **not** yet part of this slice. CRC/MD5/SHA-512 submenus are not implemented. In Windows 11, the classic menu may require **Show more options**.
- If the portable executable is moved after registration, reinstall the context menu from the new path; removing the menu deletes only UniZip-owned verb keys.

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
