# Cambia jdk21 por la version de JDK que uses en NetBeans (17, 21, etc.)
FROM tomcat:10.1-jdk21

# Quita las aplicaciones de ejemplo de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# ROOT.war hace que la app abra en la raiz: https://tu-app.onrender.com/
COPY ROOT.war /usr/local/tomcat/webapps/ROOT.war

COPY start.sh /start.sh
RUN chmod +x /start.sh

CMD ["/start.sh"]
