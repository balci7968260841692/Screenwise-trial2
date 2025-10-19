# Define paths
$androidSdkRoot = "tools/android-sdk"
$lp = "local.properties"
$lpExample = "local.properties.example"
$sdkLine = "sdk.dir=tools/android-sdk"

# Ensure the SDK root exists
if (-not (Test-Path $androidSdkRoot)) {
    New-Item -ItemType Directory -Path $androidSdkRoot | Out-Null
}

# Unzip platforms.zip → platforms/android-36/
$platformsDest = Join-Path $androidSdkRoot "platforms"
Expand-Archive -Path "tools/platforms.zip" -DestinationPath $platformsDest -Force

# Unzip build-tools.zip → build-tools/36.0.0/
$buildToolsDest = Join-Path $androidSdkRoot "build-tools"
Expand-Archive -Path "tools/build-tools.zip" -DestinationPath $buildToolsDest -Force

# Unzip platform-tools.zip → platform-tools/
$platformToolsDest = Join-Path $androidSdkRoot "platform-tools"
Expand-Archive -Path "tools/platform-tools.zip" -DestinationPath $platformToolsDest -Force

# Copy local.properties.example if local.properties doesn't exist
if (-not (Test-Path $lp) -and (Test-Path $lpExample)) {
    Copy-Item $lpExample $lp
}

# Add or update sdk.dir=tools/android-sdk
if (Test-Path $lp) {
    $lines = Get-Content $lp
    $sdkLineExists = $false

    $updatedLines = $lines | ForEach-Object {
        if ($_ -match "^sdk\.dir\s*=") {
            $sdkLineExists = $true
            $sdkLine
        } else {
            $_
        }
    }

    if (-not $sdkLineExists) {
        $updatedLines += $sdkLine
    }

    Set-Content -Path $lp -Value $updatedLines -Encoding ASCII
}
