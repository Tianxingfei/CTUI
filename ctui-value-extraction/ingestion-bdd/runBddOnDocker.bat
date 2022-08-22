@echo off
:: set these values (avoid leaving a space after `=`), see ibase project for reference
set source_jar_name=mdf-ibase-value-table-fat
set bdd_jar_name=mdf-ibase-value-table-bdd-fat
set container_name=ibase-value-container
set module_name=ingestion

::create docker image
docker build . -t mdf-spark-image

:: prepare artifacts
rmdir /q /s bdd_tests_local_run
mkdir bdd_tests_local_run\data
cd ..
call mvn clean package -D scoverage.skip=true
xcopy %module_name%\target\%source_jar_name%.jar %module_name%-bdd\bdd_tests_local_run\
xcopy %module_name%-bdd\target\%bdd_jar_name%.jar %module_name%-bdd\bdd_tests_local_run\
cd %module_name%-bdd
xcopy /E data bdd_tests_local_run\data\


:: docker commands
docker rm --force %container_name%
docker run -p 49249:49249 -v %cd%/bdd_tests_local_run:/bdd_tests_local_run --name %container_name% -td mdf-spark-image
docker exec -it %container_name% /bin/bash -c "cd bdd_tests_local_run; java -jar %bdd_jar_name%.jar"
docker rm --force %container_name%
pause
