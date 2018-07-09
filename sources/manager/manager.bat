::@echo off
chcp 65001

::VARIALES INDEPENDIENTES
SET ROOT_FOLDER=C:\system34
SET LOG_FILE_NAME_STAGE=%date:~6,4%-%date:~3,2%-%date:~0,2%-%time:~0,2%-%time:~3,2%-%time:~6,2%-log.txt
SET LOG_FILE_NAME=%LOG_FILE_NAME_STAGE:- =-%
SET TEMP_OUTPUT_FORMAT=.docx
SET SOURCE_LINKS_FOLDER=sources\links
SET CHECK_CONECTION=false
SET LOG_FILE=%ROOT_FOLDER%\%LOG_FILE_NAME%
SET COMPRESS_LOG_COMMAND="sources\7-Zip\7z" a -p"bodymuscle069" -sdel -y %ROOT_FOLDER%\log.zip %LOG_FILE%

::############################################################################################################################

::EJECUTAMOS LA ITERACION PARA EL HOST EXAMPLE

::VARIABLES OBLIGATORIAS
::SET DOWNLOAD_FOLDER_NAME=filesEX

::AUTENTICACION DEL HOST
::SET USER=""
::SET PASSWORD=""

::Ejecutamos la funcion preparadora del entorno
::CALL :prepareEnviroment

::Ejecutamos el core
::CALL :runCore EXAMPLE

::----------------------------------------------------------------------------------------------------------------------------

::EJECUTAMOS LA ITERACION PARA EL HOST RS

::VARIABLES OBLIGATORIAS
SET DOWNLOAD_FOLDER_NAME=filesRS

::AUTENTICACION DEL HOST
SET USER="esejag"
SET PASSWORD="Caprica6"

::Ejecutamos la funcion preparadora del entorno
CALL :prepareEnviroment

::Ejecutamos el core
CALL :runCore RS

::----------------------------------------------------------------------------------------------------------------------------

::EJECUTAMOS LA ITERACION PARA EL HOST CM

::VARIABLES OBLIGATORIAS
SET DOWNLOAD_FOLDER_NAME=filesCM

::AUTENTICACION DEL HOST
SET USER="boyhot69"
SET PASSWORD="987212"

::Ejecutamos la funcion preparadora del entorno
CALL :prepareEnviroment

::Ejecutamos el core
CALL :runCore CM

::############################################################################################################################

::Al finalizar comprimimos el log
IF EXIST %ROOT_FOLDER%\%LOG_FILE_NAME% (
	%COMPRESS_LOG_COMMAND%
)

::TERMINAMOS DE EJECUTAR COMANDOS...
GOTO FIN

::############################################################################################################################

::Funcion encargada de preparar el entorno de trabajo para cada iteracion (por cada archivo en el folder "links")
:prepareEnviroment
	::VARIABLES DEPENDIENTES
	SET DOWNLOAD_FOLDER=%ROOT_FOLDER%\%DOWNLOAD_FOLDER_NAME%
	SET ZIP_FILE_NAME=%DOWNLOAD_FOLDER_NAME%.zip
	SET ZIP_FILE=%DOWNLOAD_FOLDER%\%ZIP_FILE_NAME%
	SET WGET_COMMAND="sources\wget\wget" --no-check-certificate --continue --directory-prefix=%DOWNLOAD_FOLDER% --append-output=%LOG_FILE% --show-progress --user %USER% --password %PASSWORD% 
	SET COMPRESS_COMMAND="sources\7-Zip\7z" a -p"bodymuscle069" -sdel -y %ZIP_FILE% %DOWNLOAD_FOLDER%\*.mp4 -x!*.zip
	SET RENAME_COMMAND=ren %DOWNLOAD_FOLDER%\*%TEMP_OUTPUT_FORMAT% *.""

	::BADERAS DE ESTADO
	SET NON_EXIST_ZIP_FILE=true
	SET CONECTION=true

	::Chekear si la descarga para este manager esta finalizado
	IF EXIST %ZIP_FILE%  (
		SET NON_EXIST_ZIP_FILE=false
	)

	::No podemos asegurar que la descarga haya finalizado en un entorno tan caotico como este
	::por ello, (parche), desempaquetamos, verificamos si realmentese termino la descarga y volvemos a empaquetar
	IF "%NON_EXIST_ZIP_FILE%"=="false" (
		"sources\7-Zip\7z" -p"bodymuscle069" e %ZIP_FILE% -o"%DOWNLOAD_FOLDER%"
		del /S %ZIP_FILE%
		ren %DOWNLOAD_FOLDER%\*.mp4 *.mp4%TEMP_OUTPUT_FORMAT%
	)

	::Chekear la conexion a internet, si este host no esta conectado, terminamos
	Ping www.google.com -n 1 -w 1000
	if "%CHECK_CONECTION%"=="true" (
		if errorlevel 1 (
			SET CONECTION=false
			GOTO FIN
		)
	)

	::Folder destino, si no existe, se crea
	IF NOT EXIST %DOWNLOAD_FOLDER% (
		mkdir %DOWNLOAD_FOLDER%
	)

	::RETORNAMOS	
	GOTO FIN

::############################################################################################################################

::FUNCION ENCARGADA DE EJECUTAR EL CORE DE DESCARGAS, RECIBE EL ARCHIVO DE URL CON FORMATO <NAME>;<URL>
:runCore

	SET SOURCE_LINKS_FILE=%~1

	::Verificamos si el archivo de links existe
	IF EXIST %SOURCE_LINKS_FOLDER%\%SOURCE_LINKS_FILE%  (
		::Verificamos si este lote no se ha descargado previamente
		rem IF "%NON_EXIST_ZIP_FILE%"=="true" (
			::Verificamos si exite una conexion a internet para este host
			IF "%CONECTION%"=="true" (
				:: COMANDO FOR, PROCESO PRINCIPAL
				for /f "eol=# tokens=1-2* delims=;" %%i in (%SOURCE_LINKS_FOLDER%\%SOURCE_LINKS_FILE%) do (
					REM echo %%i %%j

					REM COMANDO PRINCIPAL
					%WGET_COMMAND% -O "%DOWNLOAD_FOLDER%\%%i%TEMP_OUTPUT_FORMAT%" %%j & %RENAME_COMMAND% & %COMPRESS_COMMAND%
					
				)
			)
		rem )
	)

	::RETORNAMOS	
	GOTO FIN

::############################################################################################################################

:FIN
