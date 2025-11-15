# ToDoApp

https://youtu.be/vot5Lsj8kgE

Testar o git workflow:

build.yaml:

1)Corre numa VM Linux (ubunt-latest)

2)uses: actions/checkout@v4 : Clona o repositório da commit que disparou o workflow para o workspace do runner ($GITHUB_WORKSPACE). Sem isto, não há ficheiros para compilar.

3)uses: actions/setup-java@v4 with java-version: '21' path: 'temurin' Instala e configura o JDK 21 no runner

4)run: mvn clean package ---Executa as fases do Maven até package

5)run: cp target/*.jar . Copia o(s) JAR(s) para a raiz do workspace apenas dentro do runner

6)uses: actions/upload-artifact@v4 com path: target/*.jar  Publica o(s) JAR(s) gerado(s) como artefacto de build visível para download na página da execução em Actions. 



Classe PdfExportService

Métricas principais:

LOC: 123 → classe de tamanho médio a grande.

WMC: 22 → complexidade alta (muitos métodos ou lógica densa).

CBO: 5 → acoplamento moderado com outras classes.

RFC: 67 → responde a muitas chamadas de métodos, indicando alta responsabilidade.

LCOM: 1 → baixa coesão (métodos pouco relacionados entre si).

CMI (Maintainability Index): 28.9 → baixa manutenibilidade, o que requer atenção.

Comentário:
A classe PdfExportService apresenta complexidade elevada e baixa coesão, o que a torna candidata a refatoração.
Pode estar concentrando demasiadas responsabilidades (um God Class smell).
Recomenda-se dividir a lógica em várias classes utilitárias (por exemplo, separando geração de PDF, formatação e gravação em ficheiro).
O baixo índice de manutenibilidade reforça a necessidade de modularizar e documentar melhor.

Classe DownloadPdfView

Métricas principais:

LOC: 43 → classe de tamanho reduzido.

WMC: 14 → complexidade média a alta considerando o tamanho.

CBO: 2 → baixo acoplamento, bom isolamento.

RFC: 25 → número razoável de métodos invocados.

LCOM: 1 → baixa coesão.

CMI (Maintainability Index): 42.97 → aceitável, mas perto do limite de baixa manutenibilidade.

Comentário:
DownloadPdfView é uma classe pequena mas com métodos relativamente complexos.
Pode beneficiar de extração de funções auxiliares para reduzir a complexidade ciclomática.
O acoplamento baixo é positivo; apenas deve ser melhorada a organização interna dos métodos.

Classe ExportPdfPreviewView

(valores não incluídos nas imagens, mas assume-se semelhante ao padrão das outras classes do módulo ExportPDF)

Comentário:
A classe ExportPdfPreviewView deverá ser analisada em conjunto com PdfExportService, pois ambas provavelmente partilham dependências e responsabilidades relacionadas com a pré-visualização e geração de PDFs.
Idealmente, deve ser mantida simples, tratando apenas da interface ou visualização, deixando a geração de ficheiros ao serviço principal.

🧩 Conclusão do módulo ExportPDF

O módulo exportpdf mostra sinais de acoplamento moderado e baixa coesão, especialmente na classe PdfExportService.
As métricas sugerem a necessidade de:

Reduzir o número de métodos complexos (WMC).

Aumentar a coesão (reduzir LCOM).

Reorganizar responsabilidades por classe.

