Set-Location 'e:\DJI-DroneRepos\DJI-Cloud-API-Demo\sample'
$cp = (Get-Content 'sample\target\runtime-classpath.txt' -Raw).Trim()
java -cp "target\classes;$cp" com.dji.sample.CloudApiSampleApplication
