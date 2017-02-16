; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppBaseDir "C:\projects\LogWrapper\"
#define MyAppExeLocation "C:\projects\LogWrapper\target\"
#define MyAppName "LogWrapper Suite"
#define MyAppExeName "LogWrapper.exe"
#define MyAppBatName "LogWrapper.bat"


[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{0FFDDE85-316B-4B74-BF99-CF97044502B7}
AppName=LogWrapper
AppVersion=1.0.0
;AppVerName=LogWrapper 1.5
AppPublisher=Diebold Nixdorf
AppPublisherURL=
AppSupportURL=
AppUpdatesURL=
DefaultDirName=C:\LogWrapper
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
OutputBaseFilename=LogWrapperSetup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#MyAppExeLocation}{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppBaseDir}{#MyAppBatName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppBaseDir}conf\*"; DestDir: "{app}\conf"; Flags: ignoreversion recursesubdirs createallsubdirs

; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\LogWrapper"; Filename: "{app}\{#MyAppExeName}"
Name: "{commondesktop}\LogWrapper"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon


[Run]
Filename: "{app}\LogWrapper.exe"; Description: "{cm:LaunchProgram,LogWrapper}"; Flags: nowait postinstall skipifsilent

[Registry]
Root: HKCR; Subkey: ".ctr";                             ValueData: "{#MyAppName}";          Flags: uninsdeletevalue; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "{#MyAppName}";                     ValueData: "Program {#MyAppName}";  Flags: uninsdeletekey;   ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "{#MyAppName}\DefaultIcon";             ValueData: "{app}\{#MyAppExeName},0";               ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "{#MyAppName}\shell\open\command";  ValueData: """{app}\LogWrapper.bat"" ""%1""";  ValueType: string;  ValueName: ""








