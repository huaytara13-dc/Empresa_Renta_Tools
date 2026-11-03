#!/bin/sh
# Render entrega el puerto HTTP en la variable PORT; Tomcat usa 8080 por defecto
sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/" /usr/local/tomcat/conf/server.xml
# Desactiva el puerto de apagado (8005): Render lo detectaba y le enviaba peticiones HTTP
sed -i 's/<Server port="8005"/<Server port="-1"/' /usr/local/tomcat/conf/server.xml
exec catalina.sh run
