package br.com.original.mbio.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import br.com.original.mbio.adapter.ResultadoGraficoRateioResponse;
import br.com.original.mbio.adapter.ResultadoPesquisaCBureauResponse;
import br.com.original.mbio.adapter.ResultadoPesquisaCCustoResponse;
import br.com.original.mbio.adapter.ResultadosPesquisaAnaliticaResponse;
import br.com.original.mbio.controlador.ConsultaRateio;
import br.com.original.mbio.controlador.FiltroPesquisaConsultasRateio;
import br.com.original.mbio.controlador.ProcessarSolicitacaoTela;
import br.com.original.mbio.controlador.RelatorioConsultaForcada;
import br.com.original.mbio.controlador.ResultadosPesquisaAnaliticca;
import br.com.original.mbio.controlador.dominio.AcessoExternoEnum;
import br.com.original.mbio.controlador.dominio.BureauEnum;
import br.com.original.mbio.controlador.dominio.FonteInformacaoEnum;
import br.com.original.mbio.controlador.dominio.MesesEnum;
import br.com.original.mbio.controlador.dominio.ResultadoGraficoRateio;
import br.com.original.mbio.controlador.dominio.ServicoIntegracaoEnum;
import br.com.original.mbio.controlador.dominio.ResultadoPesquisaCBureau;
import br.com.original.mbio.controlador.dominio.ResultadoPesquisaCCusto;
import br.com.original.mbio.controlador.dominio.ResultadoPesquisaTotalRateio;
import br.com.original.mbio.controlador.dominio.ServicoEnum;
import br.com.original.mbio.controlador.excecao.ConsultaNaoAutorizada;
import br.com.original.mbio.controlador.excecao.DadosDeReusoNaoEncontrado;
import br.com.original.mbio.controlador.excecao.IntegradorIndisponivel;
import br.com.original.mbio.controlador.excecao.ServicoContingenciaIndisponivel;
import br.com.original.mbio.controlador.excecao.ServicoContingenciaNaoEncontrado;
import br.com.original.mbio.controlador.excecao.SolicitacaoInvalida;

@Controller
public class ControladorConsultaRateio {

    private static final String NOVA_CONSULTA = "resultados";

    private final ConsultaRateio consultaRateio;

    
    private static final String URL_CONSULTA_RATEIO = "consultaRateio";

    @Autowired
    public ControladorConsultaRateio(ConsultaRateio consultaRateio,
            ProcessarSolicitacaoTela processarSolicitacaoTela, RelatorioConsultaForcada relatorio) {
        this.consultaRateio = consultaRateio;
    }
    
    @RequestMapping("/consultaRateio")
    public ModelAndView consultaRateio() {
        final ModelAndView mv = new ModelAndView(URL_CONSULTA_RATEIO);
        String consulta = "analitica";
        mv.addObject(NOVA_CONSULTA, new ArrayList<ConsultaRateio.ResultadosRateio>());
      
        mv.addObject("meses", MesesEnum.values());
        mv.addObject("anos", popularUltimos6Anos());
        mv.addObject("tipoConsulta", consulta);
        
        // Preencher as combos com todos os VALORES
        mv.addObject("centrosCusto", consultaRateio.centroCusto());
        mv.addObject("centroCusto", "-1");
        mv.addObject("bureau", BureauEnum.values());
        mv.addObject("bureauSelecionado", "-1");
        mv.addObject("servicos", ServicoEnum.values());
        mv.addObject("servicoSelecionado", "-1");
        mv.addObject("fonteInformacoes", FonteInformacaoEnum.values());
        mv.addObject("fonteInformacaoSelecionado", "-1");
        mv.addObject("acessoExterno", AcessoExternoEnum.values());
        mv.addObject("acessoExternoSelecionado", "-1");
        
        return mv;
    }
    
    /** Consulta Consumo Mensal Bureau - Grafico*/
    @ResponseBody
    @RequestMapping(value = "grafico", method = RequestMethod.GET)
    public ResultadoGraficoRateioResponse graficoConsumoMensalBureauServico(Model model, FiltroPesquisaConsultasRateio filtro, String flag, String pesquisar)
            throws ServicoContingenciaIndisponivel, ServicoContingenciaNaoEncontrado, IntegradorIndisponivel,
            DadosDeReusoNaoEncontrado, SolicitacaoInvalida, ConsultaNaoAutorizada, IOException {
    	String bureau = filtro.getBureau();
    	
    	List<ResultadoGraficoRateio> resultados = new ArrayList<>();
    	resultados = consultaRateio.consultaGrafico(filtro);
		
    	ResultadoGraficoRateioResponse response = new ResultadoGraficoRateioResponse();
		response.setListaGrafico(resultados);
		return response;
    }
   
    /** Consulta Rateio Analitica */
    @ResponseBody
    @RequestMapping(value = "consultaAnalitica", method = RequestMethod.GET)
    public ResultadosPesquisaAnaliticaResponse consultarRateio(Model model, FiltroPesquisaConsultasRateio filtro, String flag, String pesquisar)
            throws ServicoContingenciaIndisponivel, ServicoContingenciaNaoEncontrado, IntegradorIndisponivel,
            DadosDeReusoNaoEncontrado, SolicitacaoInvalida, ConsultaNaoAutorizada, IOException {
    	String bureau = filtro.getBureau();
    	
    	List<ResultadosPesquisaAnaliticca> resultados = new ArrayList<>();
    	resultados = consultaRateio.consulta(filtro);
		
		if("-1".equals(bureau)){
			 ServicoEnum [] servicos = ServicoEnum.values();
			for (ResultadosPesquisaAnaliticca r : resultados) {
				for(ServicoEnum s : servicos){
					if(s.getServico().equals(r.getServico())){
						r.setBureau(s.getBureau());
						break;
					}
				}
			}
		}else{
			for (ResultadosPesquisaAnaliticca r : resultados) {
				r.setBureau(bureau);
			}
		}
		
		ResultadosPesquisaAnaliticaResponse response = new ResultadosPesquisaAnaliticaResponse();
		response.setListaReultados(resultados);
		return response;
    }
    
    /** Consulta Rateio Centro Custo */
    @ResponseBody
    @RequestMapping(value = "consolidadaCCusto", method = RequestMethod.GET)
    public ResultadoPesquisaCCustoResponse consultaConsolidadaCCusto(Model model, FiltroPesquisaConsultasRateio filtro, String flag, String pesquisar)
            throws ServicoContingenciaIndisponivel, ServicoContingenciaNaoEncontrado, IntegradorIndisponivel,
            DadosDeReusoNaoEncontrado, SolicitacaoInvalida, ConsultaNaoAutorizada, IOException {
    	
    	List<ResultadoPesquisaCCusto> resultados = new ArrayList<>();
    	List<ResultadoPesquisaCCusto> listaResponse = new ArrayList<>();
    	ResultadoPesquisaTotalRateio totalRateio = new ResultadoPesquisaTotalRateio();
    	
    	resultados = consultaRateio.consultaCCusto(filtro);
    	totalRateio = consultaRateio.consultaTotalCCusto(filtro).get(0);
    	ordenarResultado(resultados);
    	
    	ResultadoPesquisaCCusto total = new ResultadoPesquisaCCusto();
    	ResultadoPesquisaCCusto centroCusto = new ResultadoPesquisaCCusto();
    	ResultadoPesquisaCCusto bureau = new ResultadoPesquisaCCusto();
    	
    	total.setCentroCusto("Total");
    	total.setExterno(totalRateio.getExterno());
    	total.setReuso(totalRateio.getReuso());
    	total.setCrivo(totalRateio.getCrivo());
    	
    	listaResponse.add(total);
    	
    	for(ResultadoPesquisaCCusto rpc : resultados){
    		if(rpc.getCentroCusto().equals(centroCusto.getCentroCusto())){
    			rpc.setCentroCusto("");
    			centroCusto.setExterno(rpc.getExterno()+centroCusto.getExterno());
    			centroCusto.setReuso(rpc.getReuso()+centroCusto.getReuso());
    			if(rpc.getBureau().equals(bureau.getBureau())){
    				rpc.setBureau("");
    				bureau.setExterno(rpc.getExterno()+bureau.getExterno());
    				bureau.setReuso(rpc.getReuso()+bureau.getReuso());
    				listaResponse.add(rpc);
    			}else{
    				bureau = new ResultadoPesquisaCCusto();
    				listaResponse.add(bureau);
    				bureau.setBureau(rpc.getBureau());
    				bureau.setExterno(rpc.getExterno());
    				bureau.setReuso(rpc.getReuso());
    				rpc.setCentroCusto("");
    				rpc.setBureau("");
    				listaResponse.add(rpc);
    			}
    		}else{
    			centroCusto = new ResultadoPesquisaCCusto();
    			listaResponse.add(centroCusto);
    			centroCusto.setCentroCusto(rpc.getCentroCusto());
    			centroCusto.setExterno(rpc.getExterno());
    			centroCusto.setReuso(rpc.getReuso());
    			
				bureau = new ResultadoPesquisaCCusto();
				listaResponse.add(bureau);
				bureau.setBureau(rpc.getBureau());
				bureau.setExterno(rpc.getExterno());
				bureau.setReuso(rpc.getReuso());
				rpc.setCentroCusto("");
				rpc.setBureau("");
				listaResponse.add(rpc);
    		}
    	}
    	ResultadoPesquisaCCustoResponse response = new ResultadoPesquisaCCustoResponse();
    	response.setListaCCusto(listaResponse);
    	
    	return response;
    }
    
    private void ordenarResultado(List<ResultadoPesquisaCCusto> response) {
        Collections.sort(response, new Comparator<ResultadoPesquisaCCusto>(){
            public int compare(ResultadoPesquisaCCusto one, ResultadoPesquisaCCusto other) {
                
            	if(!one.getCentroCusto().equals(other.getCentroCusto())){                    
                    return one.getCentroCusto().compareTo(other.getCentroCusto());
                }else{
                	if(!one.getBureau().equals(other.getBureau())){                    
                        return one.getBureau().compareTo(other.getBureau());
                    }else{
                        return one.getServico().compareTo(other.getServico());
                    }
                }
            }
        });
	}
    
    @ResponseBody
    @RequestMapping(value = "consolidadaBureau", method = RequestMethod.GET )
    public ResultadoPesquisaCBureauResponse consultaConsolidadaCCustoBureau(Model model, FiltroPesquisaConsultasRateio filtro, String flag, String pesquisar)
            throws ServicoContingenciaIndisponivel, ServicoContingenciaNaoEncontrado, IntegradorIndisponivel,
            DadosDeReusoNaoEncontrado, SolicitacaoInvalida, ConsultaNaoAutorizada, IOException {
    	
    	List<ResultadoPesquisaCBureau> resultados = new ArrayList<>();
    	resultados = consultaRateio.consultaCBureau(filtro);
    	ResultadoPesquisaCBureauResponse response = new ResultadoPesquisaCBureauResponse();
    	response.setListaCBureau(resultados);
    	return response;
    }
    
    // Listar para seleção os últimos 6 anos a partir do ano corrente, incluindo o mesmo
    public static List<Integer> popularUltimos6Anos() {
    	
	    List<Integer> listaAnos = new ArrayList<>();
	    
	    int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
	    listaAnos.add(anoAtual);
	    for (int i = 1; i <= 5; i++) {
	    	listaAnos.add(anoAtual - i);
	    }
	    return listaAnos;
	}
    
    @ResponseBody
    @RequestMapping(value = "/selecionaBureau", method = RequestMethod.GET)
    public List<String> selecionaBureau(String bureau) {
    
    	List<String> servico = new ArrayList<String>();
    	ServicoEnum[] servicos = ServicoEnum.values();
    	
    	if( "-1".equals(bureau)){
    		for (ServicoEnum servicoEnum : servicos) {
    			servico.add(servicoEnum.getServico());
    		}
    	}else{
    		for (ServicoEnum servicoEnum : servicos) {
    			if(servicoEnum.getBureau().equals(bureau)){
    				servico.add(servicoEnum.getServico());
    			}
    		}
    	}
    	
		return servico;
    }
    
    @ResponseBody
    @RequestMapping(value = "/selecionaServico", method = RequestMethod.GET)
    public List<FonteInformacaoEnum> selecionaServico(String servico) {
    	
    	List<FonteInformacaoEnum> listaFonteInformacaoEnum = new ArrayList<FonteInformacaoEnum>();

    	if( !"-1".equals(servico)){
    		ServicoIntegracaoEnum[] servicoEnum = ServicoIntegracaoEnum.values();
    		
    		for (ServicoIntegracaoEnum servicoIntegracaoEnum : servicoEnum) {
    			if(servicoIntegracaoEnum.getServico().equals(servico)){
    				listaFonteInformacaoEnum.add(servicoIntegracaoEnum.getFonteInformacao());
    			}
    		}
    	}
    	
		return listaFonteInformacaoEnum;
    }   
    
    public String preencherBureauSelecionado(String bureau) {
    	String bureauSelecionado = null;
    	BureauEnum[] bureaus = BureauEnum.values();
    	
    	for (BureauEnum bureauEnum : bureaus) {
    		if(bureauEnum.getBureau().equals(bureau)){
    			bureauSelecionado = bureauEnum.getBureau().toString();
			}
		}
		return bureauSelecionado;
    }
    
    public String preencherServicoSelecionado(String servico) {
    	String servicoSelecionado = null;
    	ServicoEnum[] servicos = ServicoEnum.values();
    	
    	for (ServicoEnum servicoEnum : servicos) {
    		if(servicoEnum.getServico().equals(servico)){
    			servicoSelecionado = servicoEnum.getServico().toString();
    		}
		}
		return servicoSelecionado;
    }
}    
