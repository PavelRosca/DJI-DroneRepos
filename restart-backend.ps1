$env:JAVA_HOME = 'E:\DJI-DroneRepos\DJI-Cloud-API-Demo\.tools\jdk-17.0.18+8'
$mvn = 'E:\DJI-DroneRepos\DJI-Cloud-API-Demo\.tools\apache-maven-3.9.9\bin\mvn.cmd'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location 'E:\DJI-DroneRepos\DJI-Cloud-API-Demo\sample'
& $mvn spring-boot:run 2>&1 | Tee-Object -FilePath 'E:\DJI-DroneRepos\backend-restart.log'
