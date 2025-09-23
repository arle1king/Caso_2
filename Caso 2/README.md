Simulador de Memoria Virtual
¿Qué hace el programa?
Simula un sistema de memoria virtual con paginación. Los procesos suman matrices y el sistema maneja page faults usando algoritmo LRU.

OPCIÓN 1: Generar Archivos de Procesos

java -cp bin VirtualMemorySimulator -g config.properties


1. Configuración básica:
echo TP=4096 > config.properties
echo NPROC=3 >> config.properties
echo TAMS=100,150,200 >> config.properties
java -cp bin VirtualMemorySimulator -g config.properties

2. Matrices pequeñas:
echo TP=2048 > config.properties
echo NPROC=2 >> config.properties
echo TAMS=50,50 >> config.properties
java -cp bin VirtualMemorySimulator -g config.properties

3. Matrices grandes:
echo TP=8192 > config.properties
echo NPROC=4 >> config.properties
echo TAMS=200,200,200,200 >> config.properties
java -cp bin VirtualMemorySimulator -g config.properties

4. Una matriz gigante:
echo TP=4096 > config.properties
echo NPROC=1 >> config.properties
echo TAMS=500 >> config.properties
java -cp bin VirtualMemorySimulator -g config.properties

5. Matrices mixtas:
echo TP=4096 > config.properties
echo NPROC=3 >> config.properties
echo TAMS=50,100,200 >> config.properties
java -cp bin VirtualMemorySimulator -g config.properties


OPCIÓN 2: Simular Ejecución

java -cp bin VirtualMemorySimulator -s <PROCESOS> <MARCOS>

1. Poca memoria (más page faults):
java -cp bin VirtualMemorySimulator -s 3 3

2. Memoria normal:
java -cp bin VirtualMemorySimulator -s 3 12

3. Mucha memoria (menos page faults):
java -cp bin VirtualMemorySimulator -s 3 24

4. 2 procesos con memoria equilibrada:
java -cp bin VirtualMemorySimulator -s 2 8

5. 4 procesos compitiendo:
java -cp bin VirtualMemorySimulator -s 4 12

6. Memoria muy limitada:
java -cp bin VirtualMemorySimulator -s 3 6

7. Proceso único con toda la memoria:
java -cp bin VirtualMemorySimulator -s 1 10

8. Muchos procesos, poca memoria por proceso:
java -cp bin VirtualMemorySimulator -s 5 10

9. Memoria óptima para 3 procesos:
java -cp bin VirtualMemorySimulator -s 3 18

10. Estrés del sistema:
java -cp bin VirtualMemorySimulator -s 6 12


🔄 Flujo típico:
# 1. Generar archivos
java -cp bin VirtualMemorySimulator -g config.properties

# 2. Simular
java -cp bin VirtualMemorySimulator -s 3 12