# SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
### Municipio de Pitalito, Huila - Implementación Integral en Java

Consulte el archivo [README.md](file:///C:/Users/Carlos%20Rojas/.gemini/antigravity/scratch/sigepac_java/README.md) para acceder a la documentación completa y detallada del proyecto.

### Resumen de comandos de ejecución:
```powershell
# 1. Compilar todo el sistema
$fuentes = Get-ChildItem -Path "1_mvc","2_capas","3_orientada_objetos","4_hexagonal","pruebas","Lanzador.java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d "bin" $fuentes

# 2. Ejecutar la suite de pruebas (34/34 superadas)
java -cp "bin" sigepac.Lanzador pruebas

# 3. Iniciar todos los servidores web simultáneamente
java -cp "bin" sigepac.Lanzador todas
```
