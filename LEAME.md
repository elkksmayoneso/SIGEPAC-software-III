# SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
### Municipio de Pitalito, Huila - Implementación Integral en Java

Consulte [README.md](README.md) para la documentación completa, incluida la configuración de MySQL Connector/J y las credenciales por arquitectura.

### Resumen de comandos de ejecución:
```powershell
# 1. Compilar todo el sistema
$fuentes = Get-ChildItem -Path "1_mvc","2_capas","3_orientada_objetos","4_hexagonal","pruebas","Lanzador.java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d "bin" $fuentes

# 2. Ejecutar la suite de pruebas (requiere MySQL/MariaDB, los cuatro esquemas y Connector/J)
java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador pruebas

# 3. Iniciar todos los servidores web simultáneamente
java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador todas
```
