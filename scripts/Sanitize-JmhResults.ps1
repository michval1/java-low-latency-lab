<#
Replace the JVM executable path in JMH JSON; leave numerical data unchanged.
Run from any directory. Does not modify JVM arguments or other metadata.
#>
$ErrorActionPreference = 'Stop'
$resultsPath = Join-Path $PSScriptRoot '..\results'
$files = Get-ChildItem -LiteralPath $resultsPath -Filter '*.json' -File
foreach ($file in $files) {
    $original = [System.IO.File]::ReadAllText($file.FullName)
    $null = $original | ConvertFrom-Json
    $sanitized = [regex]::Replace($original,
        '"jvm"\s*:\s*"[^"\r\n]*"',
        '"jvm" : "<JDK_HOME>/bin/java.exe"')
    $null = $sanitized | ConvertFrom-Json
    if ($sanitized -match '(?i)([A-Z]:\\|/Users/|/home/)') {
        throw "Other absolute paths remain in $($file.Name); inspect before committing."
    }
    if ($original -ne $sanitized) {
        [System.IO.File]::WriteAllText($file.FullName, $sanitized,
            [System.Text.UTF8Encoding]::new($false))
        Write-Output "Sanitized: $($file.Name)"
    } else {
        Write-Output "Already sanitized: $($file.Name)"
    }
}
