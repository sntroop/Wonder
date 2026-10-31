Unicode true
!include "MUI2.nsh"
!include "FileFunc.nsh"

!define APP_NAME "Wonder"
!define APP_VERSION "1.0.9"
!define APP_PUBLISHER "Gwarden"
!define APP_EXE "Wonder.exe"
!define APP_KEY "Wonder"

Name "${APP_NAME}"
OutFile "Wonder-Setup-${APP_VERSION}.exe"
InstallDir "$LOCALAPPDATA\Programs\${APP_NAME}"
InstallDirRegKey HKCU "Software\${APP_KEY}" "InstallLocation"
RequestExecutionLevel user
SetCompressor zlib

BrandingText "Wonder"

VIProductVersion "1.0.9.0"
VIAddVersionKey "ProductName" "${APP_NAME}"
VIAddVersionKey "FileDescription" "Wonder - музыка без границ"
VIAddVersionKey "FileVersion" "${APP_VERSION}"
VIAddVersionKey "ProductVersion" "${APP_VERSION}"
VIAddVersionKey "CompanyName" "${APP_PUBLISHER}"
VIAddVersionKey "LegalCopyright" "© 2026 ${APP_PUBLISHER}"

!define MUI_ICON "build/icon.ico"
!define MUI_UNICON "build/icon.ico"
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\${APP_EXE}"
!define MUI_FINISHPAGE_RUN_TEXT "Запустить Wonder"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "Russian"

Section "Wonder" SecMain
  SetOutPath "$INSTDIR"
  ; Wonder живёт в трее: закрытие окна не завершает процесс, а залоченный
  ; Wonder.exe ломает установку («Невозможно записать ... Wonder.exe»).
  ; Поэтому сначала мягко просим закрыться, потом снимаем принудительно.
  nsExec::Exec 'taskkill /IM Wonder.exe /T'
  Sleep 1200
  nsExec::Exec 'taskkill /IM Wonder.exe /T /F'
  Sleep 800
  File /r "release/Wonder-win32-x64/*.*"
  ; Проверяем, что интерфейс реально скопировался: без него приложение
  ; открывалось серым окном с JSON {"error":"not_found"}.
  IfFileExists "$INSTDIR\resources\app\web\index.html" wonder_web_ok
  IfSilent wonder_web_ok
  MessageBox MB_ICONSTOP "Wonder установлен не полностью: не скопированы файлы интерфейса ($INSTDIR\resources\app\web).$\r$\n$\r$\nЗакрой Wonder (трей -> Выход), удали эту папку и запусти установщик снова."
wonder_web_ok:
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortCut "$DESKTOP\Wonder.lnk" "$INSTDIR\${APP_EXE}"
  CreateDirectory "$SMPROGRAMS\Wonder"
  CreateShortCut "$SMPROGRAMS\Wonder\Wonder.lnk" "$INSTDIR\${APP_EXE}"
  CreateShortCut "$SMPROGRAMS\Wonder\Удалить Wonder.lnk" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKCU "Software\${APP_KEY}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "DisplayName" "${APP_NAME}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "DisplayVersion" "${APP_VERSION}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "Publisher" "${APP_PUBLISHER}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "DisplayIcon" "$INSTDIR\${APP_EXE}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "UninstallString" "$\"$INSTDIR\Uninstall.exe$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "QuietUninstallString" "$\"$INSTDIR\Uninstall.exe$\" /S"
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "NoRepair" 1
  ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
  IntFmt $0 "0x%08X" $0
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}" "EstimatedSize" "$0"
SectionEnd

Section "Uninstall"
  ; перед удалением гасим процесс, иначе Wonder.exe и часть файлов остаются
  nsExec::Exec 'taskkill /IM Wonder.exe /T'
  Sleep 1200
  nsExec::Exec 'taskkill /IM Wonder.exe /T /F'
  Sleep 800
  Delete "$DESKTOP\Wonder.lnk"
  Delete "$SMPROGRAMS\Wonder\Wonder.lnk"
  Delete "$SMPROGRAMS\Wonder\Удалить Wonder.lnk"
  RMDir "$SMPROGRAMS\Wonder"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\${APP_KEY}"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_KEY}"
SectionEnd
