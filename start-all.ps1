# start-all.ps1 - Launch full DJI Cloud API Demo stack
# Run from any directory. Opens separate windows for long-running processes.

$ErrorActionPreference = 'Continue'

function Test-Port {
    param([int]$Port)
    try {
        $conn = New-Object System.Net.Sockets.TcpClient
        $conn.Connect('127.0.0.1', $Port)
        $conn.Close()
        return $true
    } catch {
        return $false
    }
}

Write-Host "=== DJI Cloud API Demo - Full Stack Launcher ===" -ForegroundColor Cyan
Write-Host ""

# -------------------------------------------------------
# 1. Redis (port 6379)
# -------------------------------------------------------
Write-Host "[1/7] Redis (6379)..." -ForegroundColor Yellow
if (Test-Port 6379) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $redisExe = 'C:\Program Files\Redis\redis-server.exe'
    if (Test-Path $redisExe) {
        Start-Process powershell -ArgumentList '-NoExit','-Command',"& '$redisExe'" -WindowStyle Normal
        Start-Sleep -Seconds 3
        if (Test-Port 6379) { Write-Host "  Started." -ForegroundColor Green }
        else { Write-Host "  WARNING: Redis not reachable on 6379" -ForegroundColor Red }
    } else {
        Write-Host "  ERROR: redis-server.exe not found at $redisExe" -ForegroundColor Red
    }
}

# -------------------------------------------------------
# 2. Mosquitto TCP (port 1883)
# -------------------------------------------------------
Write-Host "[2/7] Mosquitto TCP (1883)..." -ForegroundColor Yellow
if (Test-Port 1883) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $mosquittoExe1883 = 'C:\Program Files\Mosquitto\mosquitto.exe'
    if (Test-Path $mosquittoExe1883) {
        Start-Process powershell -ArgumentList '-NoExit','-Command',"& '$mosquittoExe1883' -v" -WindowStyle Normal
        Start-Sleep -Seconds 3
        if (Test-Port 1883) { Write-Host "  Started." -ForegroundColor Green }
        else { Write-Host "  WARNING: Mosquitto not reachable on 1883" -ForegroundColor Red }
    } else {
        Write-Host "  ERROR: mosquitto.exe not found at $mosquittoExe1883" -ForegroundColor Red
    }
}

# -------------------------------------------------------
# 3. MariaDB (port 3306)
# -------------------------------------------------------
Write-Host "[3/7] MariaDB (3306)..." -ForegroundColor Yellow
if (Test-Port 3306) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $mariadbd = 'C:\Program Files\MariaDB 12.2\bin\mariadbd.exe'
    $myini    = 'C:\Program Files\MariaDB 12.2\data\my.ini'
    if (Test-Path $mariadbd) {
        Start-Process powershell -ArgumentList '-NoExit','-Command',"& '$mariadbd' --defaults-file='$myini' --console" -WindowStyle Normal
        Write-Host "  MariaDB window opened. Waiting..." -ForegroundColor DarkYellow
        $waited = 0
        while (-not (Test-Port 3306) -and $waited -lt 20) { Start-Sleep 2; $waited += 2 }
        if (Test-Port 3306) { Write-Host "  MariaDB up." -ForegroundColor Green }
        else { Write-Host "  WARNING: MariaDB not reachable after 20s" -ForegroundColor Red }
    } else {
        Write-Host "  ERROR: mariadbd.exe not found at $mariadbd" -ForegroundColor Red
    }
}

# -------------------------------------------------------
# 4. Mosquitto WebSocket listener (port 8083)
# -------------------------------------------------------
Write-Host "[4/7] Mosquitto WebSocket (8083)..." -ForegroundColor Yellow
if (Test-Port 8083) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $mosquittoExe = 'C:\Program Files\Mosquitto\mosquitto.exe'
    $wsConf       = 'e:\DJI-DroneRepos\mosquitto-ws.conf'
    if (Test-Path $mosquittoExe) {
        Start-Process powershell -ArgumentList '-NoExit','-Command',"& '$mosquittoExe' -v -c '$wsConf'" -WindowStyle Normal
        Start-Sleep -Seconds 3
        if (Test-Port 8083) { Write-Host "  Started." -ForegroundColor Green }
        else { Write-Host "  WARNING: Mosquitto WS not reachable on 8083" -ForegroundColor Red }
    } else {
        Write-Host "  ERROR: mosquitto.exe not found at $mosquittoExe" -ForegroundColor Red
    }
}

# -------------------------------------------------------
# 5. MinIO (port 9000)
# -------------------------------------------------------
Write-Host "[5/8] MinIO (9000)..." -ForegroundColor Yellow
if (Test-Port 9000) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $minioExe = 'E:\downloads\minio.exe'
    if (Test-Path $minioExe) {
        Start-Process powershell -ArgumentList '-NoExit','-Command',"& '$minioExe' server C:\minio-data --console-address :9001" -WindowStyle Normal
        Start-Sleep -Seconds 4
        if (Test-Port 9000) { Write-Host "  Started." -ForegroundColor Green }
        else { Write-Host "  WARNING: MinIO not reachable on 9000" -ForegroundColor Red }
    } else {
        Write-Host "  ERROR: minio.exe not found at $minioExe" -ForegroundColor Red
    }
}

# -------------------------------------------------------
# 6. MediaMTX (port 8889)
# -------------------------------------------------------
Write-Host "[6/8] MediaMTX (8889)..." -ForegroundColor Yellow
if (Test-Port 8889) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $mediamtxExe = 'C:\Users\Pavel\AppData\Local\Microsoft\WinGet\Packages\bluenviron.mediamtx_Microsoft.Winget.Source_8wekyb3d8bbwe\mediamtx.exe'
    $mediamtxDir = Split-Path $mediamtxExe
    if (Test-Path $mediamtxExe) {
        Start-Process powershell -ArgumentList '-NoExit','-Command',"Set-Location '$mediamtxDir'; .\mediamtx.exe" -WindowStyle Normal
        Start-Sleep -Seconds 4
        if (Test-Port 8889) { Write-Host "  Started." -ForegroundColor Green }
        else { Write-Host "  WARNING: MediaMTX not reachable on 8889" -ForegroundColor Red }
    } else {
        Write-Host "  ERROR: mediamtx.exe not found at $mediamtxExe" -ForegroundColor Red
    }
}

# -------------------------------------------------------
# 6. Backend Spring Boot (port 6789)
# -------------------------------------------------------
Write-Host "[7/8] Backend (6789)..." -ForegroundColor Yellow
if (Test-Port 6789) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $backendScript = 'e:\DJI-DroneRepos\start-backend.ps1'
    Start-Process powershell -ArgumentList '-ExecutionPolicy','Bypass','-NoExit','-File',$backendScript -WindowStyle Normal
    Write-Host "  Backend window opened. Waiting up to 40s for port 6789..." -ForegroundColor DarkYellow
    $waited = 0
    while (-not (Test-Port 6789) -and $waited -lt 40) { Start-Sleep 2; $waited += 2 }
    if (Test-Port 6789) { Write-Host "  Backend up." -ForegroundColor Green }
    else { Write-Host "  WARNING: Backend not reachable after 40s (may still be starting)" -ForegroundColor Red }
}

# -------------------------------------------------------
# 7. Frontend Vite (port 8080)
# -------------------------------------------------------
Write-Host "[8/8] Frontend (8080)..." -ForegroundColor Yellow
if (Test-Port 8080) {
    Write-Host "  Already running." -ForegroundColor Green
} else {
    $frontendDir = 'e:\DJI-DroneRepos\Cloud-API-Demo-Web'
    $viteCmd = "Set-Location '$frontendDir'; .\node_modules\.bin\vite.cmd --host 0.0.0.0 --port 8080 --strictPort"
    Start-Process powershell -ArgumentList '-NoExit','-Command',$viteCmd -WindowStyle Normal
    Start-Sleep -Seconds 6
    if (Test-Port 8080) { Write-Host "  Frontend up." -ForegroundColor Green }
    else { Write-Host "  WARNING: Frontend not yet reachable on 8080 (may still be starting)" -ForegroundColor Red }
}

# -------------------------------------------------------
# Final status report
# -------------------------------------------------------
Write-Host ""
Write-Host "=== Port Status ===" -ForegroundColor Cyan
$ports = @(1883, 3306, 6379, 6789, 8080, 8083, 8889, 9000)
foreach ($p in $ports) {
    $status = if (Test-Port $p) { "UP" } else { "DOWN" }
    $color  = if ($status -eq 'UP') { 'Green' } else { 'Red' }
    Write-Host ("  {0,5} : {1}" -f $p, $status) -ForegroundColor $color
}
Write-Host ""
Write-Host "Frontend: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Pilot2 login: http://192.168.1.160:8080/pilot-login" -ForegroundColor Cyan
