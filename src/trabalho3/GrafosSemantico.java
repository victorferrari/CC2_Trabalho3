package trabalho3;

import org.antlr.v4.runtime.Token;

/**
 * Created by Andre on 20/11/2017.
 */
public class GrafosSemantico extends GrafosBaseVisitor<String> {

    String grupo;
    static PilhaDeTabelas pilhaDeTabelas;
    //declaracao global de tipo para facilitar em momentos como em declaracoes de funcoes e procediemntos
    String tipo;
    SaidaParser sp;
    Mensagens errosSemanticos;

    public GrafosSemantico(SaidaParser sp){
        this.sp = sp;
        grupo = "<619922_619795_619841_552437>";
        errosSemanticos = new Mensagens(sp);
        pilhaDeTabelas = new PilhaDeTabelas();
        pilhaDeTabelas.empilhar(new TabelaDeSimbolos("global"));
    }

    // algoritmo :	'declaracoes' declaracao 'inicio' codigo 'fim' ;
    @Override
    public String visitAlgoritmo(GrafosParser.AlgoritmoContext ctx) {
        if(ctx.children != null){
            visitDeclaracao(ctx.declaracao());
            visitCodigo(ctx.codigo());
        }
        return null;
    }

    //declaracao : variavel declaracao | ;
    @Override
    public String visitDeclaracao(GrafosParser.DeclaracaoContext ctx) {
        if(ctx.children != null){
            visitVariavel(ctx.variavel());
            visitDeclaracao(ctx.declaracao());
        }
        return null;
    }

    // variavel : tipo IDENT mais_variavel ;
    @Override
    public String visitVariavel(GrafosParser.VariavelContext ctx) {
        if(ctx.children != null){
            tipo = visitTipo(ctx.tipo());

            //verifica se variavel esta na tabela de simbolos. se nao esta adiciona na tabela
            if(pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText()))
                errosSemanticos.erroVariavelJaDeclarada(ctx.start.getLine(), ctx.IDENT().getText());
            else{
                switch(tipo){
                    case "grafo" :
                        pilhaDeTabelas.topo().adicionarSimbolo(ctx.IDENT().getText(), tipo, "");
                        break;
                    case "vetor" :
                        pilhaDeTabelas.topo().adicionarSimbolo(ctx.IDENT().getText(), tipo, "");
                        break;
                    case "vertice" :
                        pilhaDeTabelas.topo().adicionarSimbolo(ctx.IDENT().getText(), tipo, "");
                        break;
                    case "int" :
                        pilhaDeTabelas.topo().adicionarSimbolo(ctx.IDENT().getText(), tipo, "false");
                        break;

                }

            }


            visitMais_variavel(ctx.mais_variavel());
        }
        return null;
    }

    // mais_variavel : ',' IDENT mais_variavel |  ;
    @Override
    public String visitMais_variavel(GrafosParser.Mais_variavelContext ctx) {
        if(ctx.children != null){

            //verifica se varivel esta na tabela de simbolos. se nao esta adiciona na tabela
            if(pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText())){
                Token token = ctx.IDENT().getSymbol();
                int line = token.getLine();
                errosSemanticos.erroVariavelJaDeclarada(line, ctx.IDENT().getText());
            }

            else
                pilhaDeTabelas.topo().adicionarSimbolo(ctx.IDENT().getText(), tipo, tipo);

            visitMais_variavel(ctx.mais_variavel());
        }

        return null;
    }

    /* tipo :	'grafo'     |
		        'vertice'   |
                'int'	    |
                'vetor'		; */
    @Override
    public String visitTipo(GrafosParser.TipoContext ctx) {
        return ctx.getText();
    }

    // codigo : (instrucoes)* ;
    @Override
    public String visitCodigo(GrafosParser.CodigoContext ctx) {
        for(int i = 0; i < ctx.instrucoes().size(); i++){
            GrafosParser.InstrucoesContext instrucoesContext = ctx.instrucoes().get(i);
            visitInstrucoes(instrucoesContext);
        }
        return null;
    }

    /*
    instrucoes :
                'aresta' '(' IDENT ',' IDENT ',' IDENT ',' INTEIRO ')'	        //grafo,vertice,vertice,peso
				| 'remove_vert' '(' IDENT ',' IDENT ')'					        //grafo,vertice
				| 'set_peso' '(' IDENT ',' IDENT ',' IDENT ')'			        //grafo,vertice,vertice
				| 'set_custo_para_vertice' '(' IDENT ',' IDENT ',' INTEIRO ')'  //grafo, vertice, valor
				| 'imprime' '(' var_ou_cadeia ')'						        //grafo, vertice, lista, etc
				| 'se' expressao 'entao' codigo senao_opcional 'fim_se'
				| 'para' IDENT 'em' vetores_para 'faca' codigo 'fim_para'
				| 'enquanto' expressao 'faca' codigo 'fim_enquanto'
				| IDENT '<-' expressao
				| 'dijkstra' '(' IDENT ',' IDENT ')'                            //grafo, verice inicia
				| 'prim' '(' IDENT ',' IDENT ')'                                //grafo, verice inicial
				| 'dfs' '(' IDENT ',' IDENT ')'							        //grafo, verice inicial
				| 'bfs' '(' IDENT ',' IDENT ')'						            //grafo, verice inicial
                | instrucoes_com_retorno
                | instrucoes_de_vetores
                ;
     */
    @Override
    public String visitInstrucoes(GrafosParser.InstrucoesContext ctx) {
        if(ctx.getText().startsWith("aresta")){

            //verifica se os parametros existem na tabela de simbolos
            if(!pilhaDeTabelas.existeSimbolo(ctx.pa_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.pa_grafo.getLine(), ctx.pa_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pa_vertice1.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.pa_vertice1.getLine(), ctx.pa_vertice1.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pa_vertice2.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.pa_vertice2.getLine(), ctx.pa_vertice2.getText());

            //verifica se a ordem dos parametros esta correta com os tipos esperados
            verifica3Paramentros(ctx.start.getLine(), ctx.pa_grafo.getText(), "grafo", ctx.pa_vertice1.getText(), "vertice",
                    ctx.pa_vertice2.getText(), "vertice");

            //verifica se 4 parametro e int
            if(ctx.int_ou_ident().IDENT() != null){
                //IDENT sera quarto parametro, entao e necessario saber se o mesmo e inteiro
                if(pilhaDeTabelas.existeSimbolo(ctx.int_ou_ident().IDENT().getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.int_ou_ident().IDENT().getText()).equals("int"))
                    errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.int_ou_ident().IDENT().getText(), "int");
                // se nao existe erro no IDENT entao e necessario verificar se ele nao e menor do que 0
                else{
                    //verifica se peso e menor do que zero
                    try{
                        String numero = ctx.int_ou_ident().IDENT().getText();
                        int valor = Integer.parseInt(numero);
                        if(valor < 0)
                            errosSemanticos.pesoNegativo(ctx.start.getLine(), ctx.int_ou_ident().IDENT().getText());
                    }catch (Exception e){

                    }

                }
            }

            //verifica se peso, caso for um numero, e menor do que zero
            if(ctx.int_ou_ident().INTEIRO() != null){
                String numero = ctx.int_ou_ident().INTEIRO().getText();
                int valor = Integer.parseInt(numero);
                if(valor < 0)
                    errosSemanticos.pesoNegativo(ctx.start.getLine(), ctx.int_ou_ident().INTEIRO().getText());

            }

            visitInt_ou_ident(ctx.int_ou_ident());
        }
        else if(ctx.getText().startsWith("remove_vert")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pr_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.pr_grafo.getLine(), ctx.pr_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pr_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.pr_vertice.getLine(), ctx.pr_vertice.getText());

            //verifica parametros
            verifica2Paramentros(ctx.start.getLine(), ctx.pr_grafo.getText(), "grafo", ctx.pr_vertice.getText(), "vertice");
        }
        else if(ctx.getText().startsWith("set_custo_para_vertice")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.ps_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.ps_grafo.getLine(), ctx.pr_vertice.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pr_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.pr_vertice.getLine(), ctx.pr_vertice.getText());

            //verifica parametros
            verifica2Paramentros(ctx.start.getLine(), ctx.ps_grafo.getText(), "grafo", ctx.ps_vertice.getText(), "vertice");

            //verifica se 3 parametro e int
            if(ctx.int_ou_ident().IDENT() != null){
                //IDENT sera terceiro parametro, entao e necessario saber se o mesmo e inteiro
                if(pilhaDeTabelas.existeSimbolo(ctx.int_ou_ident().IDENT().getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.int_ou_ident().IDENT().getText()).equals("int"))
                    errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.int_ou_ident().IDENT().getText(), "int");
                // se nao existe erro no IDENT entao e necessario verificar se ele nao e menor do que 0
                else{
                    //verifica se peso e menor do que zero
                    try{
                        String numero = ctx.int_ou_ident().IDENT().getText();
                        int valor = Integer.parseInt(numero);
                        if(valor < 0)
                            errosSemanticos.pesoNegativo(ctx.start.getLine(), ctx.int_ou_ident().IDENT().getText());
                    }catch (Exception e){

                    }

                }
            }

            //verifica se peso, caso for um numero, e menor do que zero
            if(ctx.int_ou_ident().INTEIRO() != null){
                String numero = ctx.int_ou_ident().INTEIRO().getText();
                int valor = Integer.parseInt(numero);
                if(valor < 0)
                    errosSemanticos.pesoNegativo(ctx.start.getLine(), ctx.int_ou_ident().INTEIRO().getText());

            }

            visitInt_ou_ident(ctx.int_ou_ident());
        }
        else if(ctx.getText().startsWith("imprime")){
            visitVar_ou_cadeia(ctx.var_ou_cadeia());
        }
        else if(ctx.getText().startsWith("empilha")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pem_vetor.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pem_vetor.getText());

            //verifica 1 param
            if(pilhaDeTabelas.existeSimbolo(ctx.pem_vetor.getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.pem_vetor.getText()).equals("vetor"))
                errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.pem_vetor.getText(), "vetor");

            //esse trecho de codigo verifica o tipo da primeira insercao no vetor e define o mesmo como
            //o tipo principal do vetor como o tipo da instancia do segundo parametro
            //verifica se o tipo da varivel e uma string vazia
            if(pilhaDeTabelas.topo().getTipo(ctx.pem_vetor.getText()).equals("")) {
                //se sim verifica se o tipo vem do ident
                if(ctx.int_ou_ident().IDENT()!=null &&
                        pilhaDeTabelas.existeSimbolo(ctx.int_ou_ident().IDENT().getText()))
                    //seta o tipo do vetor como o tipo do IDENT
                    pilhaDeTabelas.topo().setTipo(ctx.pem_vetor.getText(), pilhaDeTabelas.topo().gettipoVar(ctx.int_ou_ident().IDENT().getText()));
                //senao e inteiro
                else
                    pilhaDeTabelas.topo().setTipo(ctx.pem_vetor.getText(), "int");
            }
            //senao o tipo ja foi setado
            else{
                //verifica o tipo do IDENT colocado e igual ao tipo do primeiro IDENT
                if(ctx.int_ou_ident().IDENT()!= null &&
                pilhaDeTabelas.existeSimbolo(ctx.int_ou_ident().IDENT().getText()) &&
                !pilhaDeTabelas.topo().getTipo(ctx.pem_vetor.getText()).equals(pilhaDeTabelas.topo().gettipoVar(ctx.int_ou_ident().IDENT().getText()))){
                    //se nao e coloca erro
                    errosSemanticos.vetorComTiposDiferentes(ctx.start.getLine(), ctx.int_ou_ident().IDENT().getText());
                }
                //senao compara com inteiro e verifica se primeiro foi int
                else if(ctx.int_ou_ident().INTEIRO()!= null &&
                        !pilhaDeTabelas.topo().getTipo(ctx.pem_vetor.getText()).equals("int")){
                    //se nao coloca erro
                    errosSemanticos.vetorComTiposDiferentes(ctx.start.getLine(), ctx.int_ou_ident().INTEIRO().getText());
                }
            }

            visitInt_ou_ident(ctx.int_ou_ident());
        }
        else if(ctx.getText().startsWith("enfileira")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pen_vetor.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pen_vetor.getText());

            //verifica 1 param
            if(pilhaDeTabelas.existeSimbolo(ctx.pen_vetor.getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.pen_vetor.getText()).equals("vetor"))
                errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.pen_vetor.getText(), "vetor");

            //esse trecho de codigo verifica o tipo da primeira insercao no vetor e define o mesmo como
            //o tipo principal do vetor como o tipo da instancia do segundo parametro
            //verifica se o tipo da varivel e uma string vazia
            if(pilhaDeTabelas.topo().getTipo(ctx.pen_vetor.getText()).equals("")) {
                //se sim verifica se o tipo vem do ident
                if(ctx.int_ou_ident().IDENT()!=null &&
                        pilhaDeTabelas.existeSimbolo(ctx.int_ou_ident().IDENT().getText()))
                    //seta o tipo do vetor como o tipo do IDENT
                    pilhaDeTabelas.topo().setTipo(ctx.pen_vetor.getText(), pilhaDeTabelas.topo().gettipoVar(ctx.int_ou_ident().IDENT().getText()));
                    //senao e inteiro
                else
                    pilhaDeTabelas.topo().setTipo(ctx.pen_vetor.getText(), "int");
            }
            //senao o tipo ja foi setado
            else{
                //verifica o tipo do IDENT colocado e igual ao tipo do primeiro IDENT
                if(ctx.int_ou_ident().IDENT()!= null &&
                        pilhaDeTabelas.existeSimbolo(ctx.int_ou_ident().IDENT().getText()) &&
                        !pilhaDeTabelas.topo().getTipo(ctx.pen_vetor.getText()).equals(pilhaDeTabelas.topo().gettipoVar(ctx.int_ou_ident().IDENT().getText()))){
                    //se nao e coloca erro
                    errosSemanticos.vetorComTiposDiferentes(ctx.start.getLine(), ctx.int_ou_ident().IDENT().getText());
                }
                //senao compara com inteiro e verifica se primeiro foi int
                else if(ctx.int_ou_ident().INTEIRO()!= null &&
                        !pilhaDeTabelas.topo().getTipo(ctx.pen_vetor.getText()).equals("int")){
                    //se nao coloca erro
                    errosSemanticos.vetorComTiposDiferentes(ctx.start.getLine(), ctx.int_ou_ident().INTEIRO().getText());
                }
            }

            visitInt_ou_ident(ctx.int_ou_ident());
        }
        else if(ctx.getText().startsWith("se")){
            visitExpressao(ctx.expressao());
            visitCodigo(ctx.codigo());
            visitSenao_opcional(ctx.senao_opcional());
        }
        else if(ctx.getText().startsWith("para")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.para.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.para.getText());

            //TODO tipo da variavel do para deve ser igual a do vetor

            visitVetores_para(ctx.vetores_para());
            visitCodigo(ctx.codigo());
        }
        else if(ctx.getText().startsWith("enquanto")){
            visitExpressao(ctx.expressao());
            visitCodigo(ctx.codigo());
        }
        else if (ctx.atribuicao != null) {

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.atribuicao.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.atribuicao.getText());
            //pesquisa o tipo da varivavel da atribuicao e se for inteiro, troca a variavel para inicializada
            else if(pilhaDeTabelas.topo().gettipoVar(ctx.atribuicao.getText()).equals("int")){
                //inteiro setado como incializado
                pilhaDeTabelas.topo().setTipo(ctx.atribuicao.getText(), "true");
            }


            visitExpressao(ctx.expressao());
        }
        else if (ctx.getText().startsWith("dijkstra")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pd_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pd_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pd_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pd_vertice.getText());

            //verifica parametros
            verifica2Paramentros(ctx.start.getLine(), ctx.pd_grafo.getText(), "grafo", ctx.pd_vertice.getText(), "vertice");

        }
        else if(ctx.getText().startsWith("prim")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pp_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pp_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pp_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pp_vertice.getText());

            //verifica parametros
            verifica2Paramentros(ctx.start.getLine(), ctx.pp_grafo.getText(), "grafo", ctx.pp_vertice.getText(), "vertice");

        }
        else if(ctx.getText().startsWith("dfs")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pdfs_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pdfs_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pdfs_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pdfs_vertice.getText());

            //verifica parametros
            verifica2Paramentros(ctx.start.getLine(), ctx.pdfs_grafo.getText(), "grafo", ctx.pdfs_vertice.getText(), "vertice");

        }
        else if(ctx.getText().startsWith("bfs")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pbfs_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pbfs_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pbfs_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pbfs_vertice.getText());

            //verifica parametros
            verifica2Paramentros(ctx.start.getLine(), ctx.pbfs_grafo.getText(), "grafo", ctx.pbfs_vertice.getText(), "vertice");

        }
        else if(ctx.instrucoes_com_retorno() != null){
            visitInstrucoes_com_retorno(ctx.instrucoes_com_retorno());
        }
        else if(ctx.instrucoes_de_vetores() != null){
            visitInstrucoes_de_vetores(ctx.instrucoes_de_vetores());
        }

        return null;
    }

    //int_ou_ident : INTEIRO | IDENT;
    @Override
    public String visitInt_ou_ident(GrafosParser.Int_ou_identContext ctx) {
        if(ctx.INTEIRO() != null)
            return ctx.INTEIRO().getText();

        else{
            if(!pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.IDENT().getText());
            else{
                //se o simbolo existe na pilha e seu tipo e int e nao foi inicializado, ou seja tipo = false
                //entao acusa erro
                if(pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText()) &&
                        pilhaDeTabelas.topo().gettipoVar(ctx.IDENT().getText()).equals("int") &&
                            pilhaDeTabelas.topo().getTipo(ctx.IDENT().getText()).equals("false")) {
                    errosSemanticos.intNaoAtribuido(ctx.start.getLine(), ctx.IDENT().getText());
                }
            }

            return ctx.IDENT().getText();
        }
    }

    /*
        instrucoes_com_retorno :
                     //retorna em um grafo o peso de uma aresta
                    'get_peso' '(' IDENT ',' IDENT ',' IDENT ')' //grafo,vertice,vertice
                    //retorna o custo para chegar em um vertice
                    | 'get_custo_para_vertice' '(' IDENT ',' IDENT')' //grafo, vertice
                    //retorna a quantidade de vertices em um grafo
                    | 'qtde_vert' '(' IDENT ')' //grafo
                    //retorna o vertice desempilhado
                    | 'desempilha''(' IDENT ')' //vetor
                    //retorna o vertice desenfileirado
                    | 'desenfila' '(' IDENT ')' //vetor
                    ;
    */
    @Override
    public String visitInstrucoes_com_retorno(GrafosParser.Instrucoes_com_retornoContext ctx) {
        if(ctx.getText().startsWith("get_peso")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pgp_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pgp_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pgp_vertice1.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pgp_vertice1.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pgp_vertice2.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pgp_vertice2.getText());

            //verifica se a ordem dos parametros esta correta com os tipos esperados
            verifica3Paramentros(ctx.start.getLine(), ctx.pgp_grafo.getText(), "grafo", ctx.pgp_vertice1.getText(), "vertice",
                    ctx.pgp_vertice2.getText(), "vertice");

        }
        else if(ctx.getText().startsWith("get_custo_para_vertice")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pgc_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pgc_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pgc_vertice.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pgc_vertice.getText());

            //verifica se a ordem dos parametros esta correta com os tipos esperados
            verifica2Paramentros(ctx.start.getLine(), ctx.pgc_grafo.getText(), "grafo", ctx.pgc_vertice.getText(), "vertice");

        }
        else if(ctx.getText().startsWith("qtde_vert")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pqv_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pqv_grafo.getText());

            //verifica se param de qtade_vert e grafo
            if(pilhaDeTabelas.existeSimbolo(ctx.pqv_grafo.getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.pqv_grafo.getText()).equals("grafo"))
                errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.pqv_grafo.getText(), "grafo");

        }
        else if(ctx.getText().startsWith("desempilha")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pdem_vetor.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pdem_vetor.getText());

            //verifica se param de desempilha e vetor
            if(pilhaDeTabelas.existeSimbolo(ctx.pdem_vetor.getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.pdem_vetor.getText()).equals("vetor"))
                errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.pdem_vetor.getText(), "vetor");
        }
        else if(ctx.getText().startsWith("desenfila")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pden_vetor.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pden_vetor.getText());

            //verifica se param de desenfila e vetor
            //IDENT sempre sera terceiro parametro, entao e necessario saber se o mesmo e inteiro
            if(pilhaDeTabelas.existeSimbolo(ctx.pden_vetor.getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.pden_vetor.getText()).equals("vetor"))
                errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.pden_vetor.getText(), "vetor");
        }

        return null;
    }

    /*
    instrucoes_de_vetores :
                'vizinhos' '(' IDENT ',' IDENT ')'  //grafo,vertice
                | 'vertices' '(' IDENT ')' //grafo (retorna lista com vertices)
                ;
     */
    @Override
    public String visitInstrucoes_de_vetores(GrafosParser.Instrucoes_de_vetoresContext ctx) {
        if(ctx.getText().startsWith("vizinhos")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pv_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pv_grafo.getText());
            else if(!pilhaDeTabelas.existeSimbolo(ctx.pv_vertice.getText()))
                    errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pv_vertice.getText());

            //verifica se a ordem dos parametros esta correta com os tipos esperados
            verifica2Paramentros(ctx.start.getLine(), ctx.pv_grafo.getText(), "grafo", ctx.pv_vertice.getText(), "vertice");
        }
        else if(ctx.getText().startsWith("vertices")){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.pver_grafo.getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.pver_grafo.getText());

            //verifica se param de desenfila e vetor
            //IDENT sempre sera terceiro parametro, entao e necessario saber se o mesmo e inteiro
            if(pilhaDeTabelas.existeSimbolo(ctx.pver_grafo.getText()) && !pilhaDeTabelas.topo().gettipoVar(ctx.pver_grafo.getText()).equals("grafo"))
                errosSemanticos.incompatibilidadeDeParametros(ctx.start.getLine(), ctx.pver_grafo.getText(), "grafo");
        }
        return null;
    }

    // vetores_para : instrucoes_de_vetores | IDENT ;
    @Override
    public String visitVetores_para(GrafosParser.Vetores_paraContext ctx) {
        if(ctx.instrucoes_de_vetores() != null){
            visitInstrucoes_de_vetores(ctx.instrucoes_de_vetores());
        }
        else if(ctx.IDENT() != null){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.IDENT().getText());
        }

        return super.visitVetores_para(ctx);
    }

    // var_ou_cadeia : CADEIA | IDENT ;
    @Override
    public String visitVar_ou_cadeia(GrafosParser.Var_ou_cadeiaContext ctx) {
        if(ctx.IDENT() != null){
            if(!pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.IDENT().getText());
        }
        else if(ctx.CADEIA() != null){
            return ctx.CADEIA().getText();
        }

        return null;
    }

    // senao_opcional : 'senao' codigo | ;
    @Override
    public String visitSenao_opcional(GrafosParser.Senao_opcionalContext ctx) {
        if(ctx.children != null){
            if(ctx.getText().startsWith("senao")){
                visitCodigo(ctx.codigo());
            }
        }

        return null;
    }

    // expressao : exp_aritmetica op_opcional;
    @Override
    public String visitExpressao(GrafosParser.ExpressaoContext ctx) {
        if(ctx.children != null){
            visitExp_aritmetica(ctx.exp_aritmetica());
            visitOp_opcional(ctx.op_opcional());
        }

        return null;
    }

    // exp_aritmetica : termo outros_termos;
    @Override
    public String visitExp_aritmetica(GrafosParser.Exp_aritmeticaContext ctx) {
        if(ctx.children != null){
            visitTermo(ctx.termo());
            visitOutros_termos(ctx.outros_termos());
        }
        return null;
    }

    // outros_termos : op_adicao termo outros_termos | ;
    @Override
    public String visitOutros_termos(GrafosParser.Outros_termosContext ctx) {
        if(ctx.children != null){
            visitOp_adicao(ctx.op_adicao());
            visitTermo(ctx.termo());
            visitOutros_termos(ctx.outros_termos());
        }

        return null;
    }

    // termo : fator outros_fatores;
    @Override
    public String visitTermo(GrafosParser.TermoContext ctx) {
        if(ctx.children != null){
            visitFator(ctx.fator());
            visitOutros_fatores(ctx.outros_fatores());
        }

        return null;
    }

    // outros_fatores: op_multiplicacao fator outros_fatores | ;
    @Override
    public String visitOutros_fatores(GrafosParser.Outros_fatoresContext ctx) {
        if(ctx.children != null){
            visitOp_multiplicacao(ctx.op_multiplicacao());
            visitFator(ctx.fator());
            visitOutros_fatores(ctx.outros_fatores());
        }

        return null;
    }

    // fator : parcela outras_parcelas;
    @Override
    public String visitFator(GrafosParser.FatorContext ctx) {
        if(ctx.children != null){
            visitParcela(ctx.parcela());
            visitOutras_parcelas(ctx.outras_parcelas());
        }

        return null;
    }

    // outras_parcelas : '%' parcela outras_parcelas | ;
    @Override
    public String visitOutras_parcelas(GrafosParser.Outras_parcelasContext ctx) {
        if(ctx.children != null){
            visitParcela(ctx.parcela());
            visitOutras_parcelas(ctx.outras_parcelas());
        }
        return super.visitOutras_parcelas(ctx);
    }

    // parcela : IDENT | INTEIRO | instrucoes_com_retorno | '(' expressao ')';
    @Override
    public String visitParcela(GrafosParser.ParcelaContext ctx) {
        if(ctx.IDENT() != null){

            //verifica se os parametros foram declarados
            if(!pilhaDeTabelas.existeSimbolo(ctx.IDENT().getText()))
                errosSemanticos.erroVariavelNaoExiste(ctx.start.getLine(), ctx.IDENT().getText());
        }
        else if(ctx.INTEIRO() != null){

        }
        else if(ctx.instrucoes_com_retorno() != null){
            visitInstrucoes_com_retorno(ctx.instrucoes_com_retorno());
        }
        else if(ctx.instrucoes_de_vetores() != null){
            visitInstrucoes_de_vetores(ctx.instrucoes_de_vetores());
        }
        else if(ctx.getText().startsWith("(")){
            visitExpressao(ctx.expressao());
        }

        return null;
    }

    // op_opcional : op_relacional exp_aritmetica | ;
    @Override
    public String visitOp_opcional(GrafosParser.Op_opcionalContext ctx) {
        if(ctx.children != null){
            visitOp_relacional(ctx.op_relacional());
            visitExp_aritmetica(ctx.exp_aritmetica());
        }

        return null;
    }

    // op_relacional : '=' | '<>' | '>=' | '<=' | '>' | '<';
    @Override
    public String visitOp_relacional(GrafosParser.Op_relacionalContext ctx) {
        return ctx.getText();
    }

    // op_multiplicacao : '*' | '/';
    @Override
    public String visitOp_multiplicacao(GrafosParser.Op_multiplicacaoContext ctx) {
        return ctx.getText();
    }

    // op_adicao : '+' | '-';
    @Override
    public String visitOp_adicao(GrafosParser.Op_adicaoContext ctx) {
        return ctx.getText();
    }


    //verifica se os tipos dos parametros estao conforme documentados na gramatica e na documentacao da linguagem
    //e necessario verificar se existe o simbolo na tabela primeiro para o caso de nao ter sido declarado
    //logo, nao estara na tabela de simbolos, nao tera tipo e ocorreta um NullPointerException

    /**
     * Funcao que verifica se os tipos esperados como parametros estao corretos
     * @param numLinha numero do erro da linha
     * @param param1 texto do parametro1
     * @param tipoEsperado1 tipo esperado do parametro1
     * @param param2 texto do parametro2
     * @param tipoEsperado2 tipo esperado do parametro2
     */
    public void verifica2Paramentros(int numLinha, String param1, String tipoEsperado1, String param2, String tipoEsperado2){
        if(pilhaDeTabelas.existeSimbolo(param1) && !pilhaDeTabelas.topo().gettipoVar(param1).equals(tipoEsperado1))
            errosSemanticos.incompatibilidadeDeParametros(numLinha, param1, tipoEsperado1);
        else if(pilhaDeTabelas.existeSimbolo(param2) && !pilhaDeTabelas.topo().gettipoVar(param2).equals(tipoEsperado2))
            errosSemanticos.incompatibilidadeDeParametros(numLinha, param2, tipoEsperado2);
    }

    //funcao analoga a anterior
    public void verifica3Paramentros(int numLinha, String param1, String tipoEsperado1, String param2, String tipoEsperado2,
                                     String param3, String tipoEsperado3){
        if(pilhaDeTabelas.existeSimbolo(param1) && !pilhaDeTabelas.topo().gettipoVar(param1).equals(tipoEsperado1))
            errosSemanticos.incompatibilidadeDeParametros(numLinha, param1, tipoEsperado1);
        else if(pilhaDeTabelas.existeSimbolo(param2) && !pilhaDeTabelas.topo().gettipoVar(param2).equals(tipoEsperado2))
            errosSemanticos.incompatibilidadeDeParametros(numLinha, param2, tipoEsperado2);
        else if(pilhaDeTabelas.existeSimbolo(param3) && !pilhaDeTabelas.topo().gettipoVar(param3).equals(tipoEsperado3))
            errosSemanticos.incompatibilidadeDeParametros(numLinha, param3, tipoEsperado3);
    }
}
