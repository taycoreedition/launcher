TAYSON TV LAUNCHER - Java

Atualização 2.0: Pinned Apps / Favorites em banners.

Funcionalidades:
- O utilizador escolhe manualmente os apps afixados.
- Máximo de 6 banners; o botão Adicionar aparece de 0 a 5.
- Ao chegar a 6, Adicionar desaparece; volta após remover um.
- Impede duplicados.
- Guarda os package names em SharedPreferences.
- Usa o ícone real do pacote e o nome da aplicação.
- Clique/OK abre a aplicação.
- Pressão longa num banner abre Substituir, Remover e mover esquerda/direita.
- Apps no rodapé abre a lista de aplicações instaladas.
- MediaListener observa instalação/remoção/substituição de pacotes.
- Hora/data atualizadas automaticamente.
- Fundo e ondas desenhados em Canvas, sem imagens pesadas.

Compatibilidade:
- minSdk 21 (Android 5.0+), pensado também para Android 7.1.2.
- targetSdk 25 para manter compatibilidade com TV Boxes antigas.
- Projeto Java nativo sem dependências externas.

Como gerir os banners:
1. No launcher, selecionar Adicionar e escolher uma aplicação.
2. Para gerir um banner, manter OK/Enter pressionado (ou long-press).
3. Escolher Substituir, Remover ou mover para a esquerda/direita.

Nota:
- O banner utiliza o ícone real fornecido pelo Android e o nome do app.
- O fundo de cada banner é um gradiente do launcher; não é um banner remoto.
- O projeto fonte precisa ser compilado no Android Studio para gerar APK.
