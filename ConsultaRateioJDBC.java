package br.com.original.mbio.infraestrutura.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import br.com.original.mbio.controlador.ConsultaRateio;
import br.com.original.mbio.controlador.FiltroPesquisaConsultasRateio;
import br.com.original.mbio.controlador.ResultadosPesquisaAnaliticca;
import br.com.original.mbio.controlador.dominio.ResultadoGraficoRateio;
import br.com.original.mbio.controlador.dominio.ResultadoPesquisaCBureau;
import br.com.original.mbio.controlador.dominio.ResultadoPesquisaCCusto;
import br.com.original.mbio.controlador.dominio.ResultadoPesquisaTotalRateio;
import br.com.original.mbio.controlador.dominio.ServicoEnum;
import br.com.original.mbio.controlador.dominio.ServicoIntegracaoEnum;
import br.com.original.mbio.controlador.util.MBIOLogger;
import br.com.original.mbio.controlador.util.UtilControlador;

public class ConsultaRateioJDBC implements ConsultaRateio {

    private static final String SQL_SELECT_ORIGEM = "SELECT NOM_ORIGE_INFO FROM MBIO.ORIGE_INFO";

    private static final String SQL_SELECT_CENTRO_CUSTO = "SELECT DES_CNTRO_CUSTO FROM MBIO.CNTRO_CUSTO ORDER BY 1";    
    
    private static final String SQL_SELECT_ORIGEM_CONSULTA_RATEIO = "SELECT NOM_ORIGE_INFO FROM MBIO.ORIGE_INFO "
            + "WHERE NOM_ORIGE_INFO <> 'Crivo' ";
    
    private static final String SQL_SELECT_CONSOLIDADA_BUREAU = "SELECT DISTINCT CC.DES_CNTRO_CUSTO, SBI.NOM_INTGC, SBI.NOM_SERVC, SBI.COD_ORIGE_INFO, CI.COD_TPO_CONS," +
 	"CASE " +
		"WHEN CI.COD_TPO_CONS <> 2 THEN " +
		"	COUNT(CI.COD_TPO_CONS) " +
		"END AS BUREAU, " +
	"CASE " +
		"WHEN CI.COD_TPO_CONS = 2 THEN " +
			"COUNT(CI.COD_TPO_CONS) " +
		"END AS REUSO, " +
	"CASE " +
		"WHEN SBI.COD_ORIGE_INFO = 1  THEN " +
			"COUNT(SBI.COD_ORIGE_INFO) " +
		"END AS CRIVO " +
	"FROM " +
		"MBIO.SERVC_BUSCA_INFO SBI, " +
		"MBIO.CONS_INFO CI, " +
		"CNTRO_CUSTO CC " +
		"WHERE CI.COD_SERVC = SBI.COD_SERVC " +
		"AND CC.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO ";
    
    private static final String SQL_SELECT_GRAFICO = "SELECT DISTINCT " + 
    "CC.DES_CNTRO_CUSTO, " +
    "SBI.NOM_INTGC, " +
    "SBI.NOM_SERVC, " +
    "TO_CHAR(CI.HOR_CONS_INFO,'MM/YY'), " +
    "(SELECT  " +
            "COUNT(CIS.COD_CONS_INFO) " +
       "FROM " +
            "CONS_INFO CIS, " +
            "CNTRO_CUSTO CCS, " +
            "SERVC_BUSCA_INFO SBIS, " +
            "ORIGE_INFO OIS " +
       "WHERE " +
            "1 = 1 " +
        "AND SBI.NOM_INTGC = SBIS.NOM_INTGC " +
        "AND CCS.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO " +
        "AND CCS.COD_CNTRO_CUSTO = CIS.COD_CNTRO_CUSTO " +
        "AND SBIS.COD_SERVC = CIS.COD_SERVC " +
        "AND SBIS.COD_ORIGE_INFO = OIS.COD_ORIGE_INFO) as ACESSOS " +
	"FROM " +
	    "CONS_INFO CI, " +
	    "CNTRO_CUSTO CC, " +
	    "SERVC_BUSCA_INFO SBI, " +
	    "ORIGE_INFO OI " +
	"WHERE 1 = 1 " +
	"AND CC.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO " +
	"AND SBI.COD_SERVC = CI.COD_SERVC " +
	"AND SBI.COD_ORIGE_INFO = OI.COD_ORIGE_INFO " +
	"ORDER BY " +
	    "TO_CHAR(CI.HOR_CONS_INFO,'MM/YY') asc ";
       
   
    
    private final JdbcTemplate jdbcTemplate;

    private final MBIOLogger mbioLogger = new MBIOLogger();

    public ConsultaRateioJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> centroCusto() {
        try {
            mbioLogger.info("Consultar Centro de Custo", "Pesquisa de Centro de Custo realizada.");
            return jdbcTemplate.queryForList(SQL_SELECT_CENTRO_CUSTO, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> origens(boolean acessoDireto) {
        if (acessoDireto) {
            return retornarOrigens(SQL_SELECT_ORIGEM_CONSULTA_RATEIO);
        }

        return retornarOrigens(SQL_SELECT_ORIGEM);
    }

    private final List<String> retornarOrigens(String query) {
        query += " ORDER BY 1";

        try {
            mbioLogger.info("Consultar Origem", "Pesquisa das Origens realizada.");
            return jdbcTemplate.queryForList(query, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    
    @Override
    public List<ResultadosPesquisaAnaliticca> consulta(FiltroPesquisaConsultasRateio parametros) {
    	Object[] parameters = null;
		String query =    " SELECT DISTINCT CI.HOR_CONS_INFO,"
						+ " CC.DES_CNTRO_CUSTO,"
						+ " CI.COD_LOGIN_USUAR,"
						+ " CI.NUM_CPF_CNPJ,"
						+ " CI.COD_TPO_CONS,"
						+ " SBI.NOM_SERVC,"
						+ " SBI.NOM_INTGC,"						
						+ " (CASE WHEN OI.IND_CRIVO = 1 THEN 'Crivo' ELSE 'Direta' END) as FONTE_INFORMACAO"
						+ " FROM CONS_INFO CI, CNTRO_CUSTO CC, SERVC_BUSCA_INFO SBI, ORIGE_INFO OI"
						+ " WHERE 1 = 1"
						+ " AND CC.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO"
						+ " AND SBI.COD_SERVC = CI.COD_SERVC"
						+ " AND SBI.COD_ORIGE_INFO = OI.COD_ORIGE_INFO";
				
				//Filtro por data
				if(!UtilControlador.isNullOrEmpty(parametros.getPeriodoInicio()) && !UtilControlador.isNullOrEmpty(parametros.getPeriodoFinal())){
					query += " AND CI.HOR_CONS_INFO BETWEEN TO_DATE('"+ parametros.getPeriodoInicio() +"','DD/MM/YYYY') AND TO_DATE('"+ parametros.getPeriodoFinal() +" 23:59:59','DD/MM/YYYY HH24:mi:ss') ";
				}
				
				//Filtro Mes/Ano
				if(!UtilControlador.isNullOrEmpty(parametros.getMes()) && !UtilControlador.isNullOrEmpty(parametros.getAno())){
					query += " AND TO_CHAR(CI.HOR_CONS_INFO, 'MM/YYYY') = '"+parametros.getMes()+"/"+parametros.getAno()+"'";
				}
				
				//Filtro Centro de custo
				if(!"-1".equals(parametros.getCentroCusto())){					
					query += " AND CC.DES_CNTRO_CUSTO = '"+ parametros.getCentroCusto() +"' ";
				}
				
				//Filtro Usuario
				if(!"".equals(parametros.getUsuario())){
					query += " AND UPPER(CI.COD_LOGIN_USUAR) = UPPER('"+ parametros.getUsuario() +"') ";
				}
				
				//Filtro Bureau
				if(!"-1".equals(parametros.getBureau())){
					query += " AND SBI.NOM_INTGC IN "+UtilControlador.listINSQLString(criaListaIntegracao(parametros.getBureau()));
				}
				
				//Filtro Servico
				if(!"-1".equals(parametros.getServico())){
					query += " AND SBI.NOM_INTGC IN "+UtilControlador.listINSQLString(getIntegracao(parametros.getServico()));
				}

				//Filtro Fonte de Informacao
				if("1".equals(parametros.getFonteInformacao())){					
					query += " AND SBI.COD_ORIGE_INFO = "+ parametros.getFonteInformacao();
				}else if("0".equals(parametros.getFonteInformacao())){
					query += " AND SBI.COD_ORIGE_INFO <> 1 ";
				}
				
				//Filtra por Acesso Externo
				if("1".equals(parametros.getAcessoExterno())){
					query += " AND CI.COD_TPO_CONS <> 2  ";
				}else if("2".equals(parametros.getAcessoExterno())){
					query += " AND CI.COD_TPO_CONS = 2  ";
				}
				
				query += " ORDER BY CI.HOR_CONS_INFO DESC";
				
		 try {
	            return jdbcTemplate.query(query, parameters, new RowMapper<ResultadosPesquisaAnaliticca>() {
	                @Override
	                public ResultadosPesquisaAnaliticca mapRow(ResultSet rs, int rowNum) throws SQLException {
	                    return retornarConsultaRateio(rs);
	                }
	            });
	        } catch (Exception e) {
	            mbioLogger.error("Pesquisa de Consulta Rateio Análitica", e);
	            return new ArrayList<>();
	        }
    }
    
    private List<String> criaListaIntegracao(String bureau){
    	List<String> servico = new ArrayList<>();
		List<String> integracao = new ArrayList<>();
		ServicoEnum[] se = ServicoEnum.values();
		ServicoIntegracaoEnum[] oie = ServicoIntegracaoEnum.values();
		
		for(ServicoEnum x : se){
			if(x.getBureau().equals(bureau)){
				servico.add(x.getServico());
			}
		}
		
		for(String s : servico){
			for(ServicoIntegracaoEnum y : oie){
				if(y.getServico().equals(s)){
					integracao.add(y.getNomeIntegracao());
				}
			}
		}
		
		return integracao;
    }
    
    private List<String> getIntegracao(String servico){
    	List<String> integracao = new ArrayList<>();
    	ServicoIntegracaoEnum[] oie = ServicoIntegracaoEnum.values();
		for(ServicoIntegracaoEnum y : oie){
			if(y.getServico().equals(servico)){
				integracao.add(y.getNomeIntegracao());
			}
		}
		return integracao;
    }
   
	
	private ResultadosPesquisaAnaliticca retornarConsultaRateio(ResultSet rs) throws SQLException {
        int rowNum = 1;

        Date data = rs.getDate(rowNum++);
        String centroCusto = rs.getString(rowNum++);
        String usuario = rs.getString(rowNum++);
        String cpfCnpj = rs.getString(rowNum++);
        Integer reuso = rs.getInt(rowNum++);
        String servico = rs.getString(rowNum++);
        String integracao = rs.getString(rowNum++);
        String fonteInfo = rs.getString(rowNum++);

        return new ResultadosPesquisaAnaliticca(data, centroCusto , usuario, cpfCnpj, reuso, servico, integracao, fonteInfo);
    }
	
	
	/** Pesquisa Consolidada por Centro de Custo */
	@Override
    @Transactional(readOnly = true)
	public List<ResultadoPesquisaCCusto> consultaCCusto(FiltroPesquisaConsultasRateio parametros) {
		Object[] param = { };
		
		String filtros = "";
		String filtrosSub = "";
		
		//Filtro por data
		if(!UtilControlador.isNullOrEmpty(parametros.getPeriodoInicio()) && !UtilControlador.isNullOrEmpty(parametros.getPeriodoFinal())){
			filtros += " AND CI.HOR_CONS_INFO BETWEEN TO_DATE('"+ parametros.getPeriodoInicio() +"','DD/MM/YYYY') AND TO_DATE('"+ parametros.getPeriodoFinal() +" 23:59:59','DD/MM/YYYY HH24:mi:ss') ";
			filtrosSub += " AND CIS.HOR_CONS_INFO BETWEEN TO_DATE('"+ parametros.getPeriodoInicio() +"','DD/MM/YYYY') AND TO_DATE('"+ parametros.getPeriodoFinal() +" 23:59:59','DD/MM/YYYY HH24:mi:ss') ";
		}
		
		//Filtro Mes/Ano
		if(!UtilControlador.isNullOrEmpty(parametros.getMes()) && !UtilControlador.isNullOrEmpty(parametros.getAno())){
			filtros += " AND TO_CHAR(CI.HOR_CONS_INFO, 'MM/YYYY') = '"+parametros.getMes()+"/"+parametros.getAno()+"'";
			filtrosSub += " AND TO_CHAR(CIS.HOR_CONS_INFO, 'MM/YYYY') = '"+parametros.getMes()+"/"+parametros.getAno()+"'";
		}
		
		//Filtro Centro de custo
		if(!"-1".equals(parametros.getCentroCusto())){					
			filtros += " AND CC.DES_CNTRO_CUSTO = '"+ parametros.getCentroCusto() +"' ";
			filtrosSub += " AND CCS.DES_CNTRO_CUSTO = '"+ parametros.getCentroCusto() +"' ";
		}
		
		//Filtro Usuario
		if(!"".equals(parametros.getUsuario())){
			filtros += " AND UPPER(CI.COD_LOGIN_USUAR) = UPPER('"+ parametros.getUsuario() +"') ";
			filtrosSub += " AND UPPER(CIS.COD_LOGIN_USUAR) = UPPER('"+ parametros.getUsuario() +"') ";
		}
		
		//Filtro Bureau
		if(!"-1".equals(parametros.getBureau())){
			filtros += " AND SBI.NOM_INTGC IN "+UtilControlador.listINSQLString(criaListaIntegracao(parametros.getBureau()));
			filtrosSub += " AND SBIS.NOM_INTGC IN "+UtilControlador.listINSQLString(criaListaIntegracao(parametros.getBureau()));
		}
		
		//Filtro Servico
		if(!"-1".equals(parametros.getServico())){
			filtros += " AND SBI.NOM_INTGC IN "+UtilControlador.listINSQLString(getIntegracao(parametros.getServico()));
			filtrosSub += " AND SBIS.NOM_INTGC IN "+UtilControlador.listINSQLString(getIntegracao(parametros.getServico()));
		}

		//Filtro Fonte de Informacao
		if(!"-1".equals(parametros.getFonteInformacao())){					
			filtros += " AND SBI.COD_ORIGE_INFO = "+ parametros.getFonteInformacao();
			filtrosSub += " AND SBIS.COD_ORIGE_INFO = "+ parametros.getFonteInformacao();
		}
		
		//Filtra por Acesso Externo
		if("1".equals(parametros.getAcessoExterno())){
			filtros += " AND CI.COD_TPO_CONS <> 2  ";
			filtrosSub += " AND CIS.COD_TPO_CONS <> 2  ";
		}else if("2".equals(parametros.getAcessoExterno())){
			filtros += " AND CI.COD_TPO_CONS = 2  ";
			filtrosSub += " AND CIS.COD_TPO_CONS = 2  ";
		}
		
		String subQueryExterno =  " SELECT COUNT(CIS.COD_CONS_INFO) " +
							" FROM CONS_INFO CIS, " +
							" CNTRO_CUSTO CCS, " +
							" SERVC_BUSCA_INFO SBIS, " +
							" ORIGE_INFO OIS " +
							" WHERE CIS.COD_TPO_CONS <> 2 " +
							" AND SBI.NOM_INTGC = SBIS.NOM_INTGC " +
							" AND CIS.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO " +
							" AND CCS.COD_CNTRO_CUSTO = CIS.COD_CNTRO_CUSTO " +
							" AND SBIS.COD_SERVC = CIS.COD_SERVC " +
							" AND SBIS.COD_ORIGE_INFO = OIS.COD_ORIGE_INFO " + filtrosSub;
			
		String subQueryReuso =  " SELECT COUNT(CIS.COD_CONS_INFO) " +
							" FROM CONS_INFO CIS, " +
							" CNTRO_CUSTO CCS, " +
							" SERVC_BUSCA_INFO SBIS, " +
							" ORIGE_INFO OIS " +
							" WHERE CIS.COD_TPO_CONS = 2 " +
							" AND SBI.NOM_INTGC = SBIS.NOM_INTGC " +
							" AND CIS.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO " +
							" AND CCS.COD_CNTRO_CUSTO = CIS.COD_CNTRO_CUSTO " +
							" AND SBIS.COD_SERVC = CIS.COD_SERVC " +
							" AND SBIS.COD_ORIGE_INFO = OIS.COD_ORIGE_INFO " + filtrosSub;
		
		String query =  "	SELECT DISTINCT	" +
				 		"	CC.DES_CNTRO_CUSTO, " +
						"	SBI.NOM_INTGC, " +
						"   SBI.NOM_SERVC, " +
						"   ("+subQueryExterno+") as EXTERNO, " +
						"   ("+subQueryReuso+") as REUSO " +
						"	FROM " +
						"   CONS_INFO CI," +
						"   CNTRO_CUSTO CC, " +
						"   SERVC_BUSCA_INFO SBI, " +
						"   ORIGE_INFO OI " +
						"   WHERE 1 = 1 " +
						"	AND CC.COD_CNTRO_CUSTO = CI.COD_CNTRO_CUSTO  " +
						"   AND SBI.COD_SERVC = CI.COD_SERVC  " +
						"   AND SBI.COD_ORIGE_INFO = OI.COD_ORIGE_INFO " + filtros;
		
		query += " ORDER BY CC.DES_CNTRO_CUSTO ";
				
        try {
            return jdbcTemplate.query(query, param, new RowMapper<ResultadoPesquisaCCusto>() {
                @Override
                public ResultadoPesquisaCCusto mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return retornarConsultaRateioCCusto(rs);
                }
            });
        } catch (Exception e) {
            mbioLogger.error("Consulta Rateio Consolidada por Centro de Custo ", e);
            return new ArrayList<>();
        }
	}
	
	private ResultadoPesquisaCCusto retornarConsultaRateioCCusto(ResultSet rs) throws SQLException {
        int rowNum = 1;

        String centroCusto = rs.getString(rowNum++);
        String nomeIntegracao = rs.getString(rowNum++);
        String servico = rs.getString(rowNum++);
        int externo = rs.getInt(rowNum++);
        int reuso = rs.getInt(rowNum++); 

        return new ResultadoPesquisaCCusto(centroCusto, nomeIntegracao, servico, externo, reuso);
 }
	
	/** Pesquisa Total por Centro de Custo */
	@Override
	@Transactional(readOnly = true)
	public List<ResultadoPesquisaTotalRateio> consultaTotalCCusto(FiltroPesquisaConsultasRateio parametros) {
		Object[] param = { };
		
		String filtrosSub = "";
		
		//Filtro por data
		if(!UtilControlador.isNullOrEmpty(parametros.getPeriodoInicio()) && !UtilControlador.isNullOrEmpty(parametros.getPeriodoFinal())){
			filtrosSub += " AND CIS.HOR_CONS_INFO BETWEEN TO_DATE('"+ parametros.getPeriodoInicio() +"','DD/MM/YYYY') AND TO_DATE('"+ parametros.getPeriodoFinal() +" 23:59:59','DD/MM/YYYY HH24:mi:ss') ";
		}
		
		//Filtro Mes/Ano
		if(!UtilControlador.isNullOrEmpty(parametros.getMes()) && !UtilControlador.isNullOrEmpty(parametros.getAno())){
			filtrosSub += " AND TO_CHAR(CIS.HOR_CONS_INFO, 'MM/YYYY') = '"+parametros.getMes()+"/"+parametros.getAno()+"'";
		}
		
		//Filtro Centro de custo
		if(!"-1".equals(parametros.getCentroCusto())){					
			filtrosSub += " AND CCS.DES_CNTRO_CUSTO = '"+ parametros.getCentroCusto() +"' ";
		}
		
		//Filtro Usuario
		if(!"".equals(parametros.getUsuario())){
			filtrosSub += " AND UPPER(CIS.COD_LOGIN_USUAR) = UPPER('"+ parametros.getUsuario() +"') ";
		}
		
		//Filtro Bureau
		if(!"-1".equals(parametros.getBureau())){
			filtrosSub += " AND SBIS.NOM_INTGC IN "+UtilControlador.listINSQLString(criaListaIntegracao(parametros.getBureau()));
		}
		
		//Filtro Servico
		if(!"-1".equals(parametros.getServico())){
			filtrosSub += " AND SBIS.NOM_INTGC IN "+UtilControlador.listINSQLString(getIntegracao(parametros.getServico()));
		}
		
		//Filtro Fonte de Informacao
		if(!"-1".equals(parametros.getFonteInformacao())){					
			filtrosSub += " AND SBIS.COD_ORIGE_INFO = "+ parametros.getFonteInformacao();
		}
		
		//Filtra por Acesso Externo
		if("1".equals(parametros.getAcessoExterno())){
			filtrosSub += " AND CIS.COD_TPO_CONS <> 2  ";
		}else if("2".equals(parametros.getAcessoExterno())){
			filtrosSub += " AND CIS.COD_TPO_CONS = 2  ";
		}
		
		String subQueryExterno =  " SELECT COUNT(CIS.COD_CONS_INFO) " +
				" FROM CONS_INFO CIS, " +
				" CNTRO_CUSTO CCS, " +
				" SERVC_BUSCA_INFO SBIS, " +
				" ORIGE_INFO OIS " +
				" WHERE CIS.COD_TPO_CONS <> 2 " +
				" AND CCS.COD_CNTRO_CUSTO = CIS.COD_CNTRO_CUSTO " +
				" AND SBIS.COD_SERVC = CIS.COD_SERVC " +
				" AND SBIS.COD_ORIGE_INFO = OIS.COD_ORIGE_INFO " + filtrosSub;
		
		String subQueryReuso =  " SELECT COUNT(CIS.COD_CONS_INFO) " +
				" FROM CONS_INFO CIS, " +
				" CNTRO_CUSTO CCS, " +
				" SERVC_BUSCA_INFO SBIS, " +
				" ORIGE_INFO OIS " +
				" WHERE CIS.COD_TPO_CONS = 2 " +
				" AND CCS.COD_CNTRO_CUSTO = CIS.COD_CNTRO_CUSTO " +
				" AND SBIS.COD_SERVC = CIS.COD_SERVC " +
				" AND SBIS.COD_ORIGE_INFO = OIS.COD_ORIGE_INFO " + filtrosSub;
		
		String subQueryCrivo =  " SELECT COUNT(OIS.COD_ORIGE_INFO) " +
				" FROM CONS_INFO CIS, " +
				" CNTRO_CUSTO CCS, " +
				" SERVC_BUSCA_INFO SBIS, " +
				" ORIGE_INFO OIS " +
				" WHERE OIS.COD_ORIGE_INFO = 1 " +
				" AND CCS.COD_CNTRO_CUSTO = CIS.COD_CNTRO_CUSTO " +
				" AND SBIS.COD_SERVC = CIS.COD_SERVC " +
				" AND SBIS.COD_ORIGE_INFO = OIS.COD_ORIGE_INFO " + filtrosSub;
		
		String query =  "	SELECT " +
				"   ("+subQueryExterno+") as EXTERNO, " +
				"   ("+subQueryReuso+") as REUSO, " +
				"   ("+subQueryCrivo+") as CRIVO " +
				"	FROM DUAL";
		
		
		try {
			return jdbcTemplate.query(query, param, new RowMapper<ResultadoPesquisaTotalRateio>() {
				@Override
				public ResultadoPesquisaTotalRateio mapRow(ResultSet rs, int rowNum) throws SQLException {
					return retornarConsultaTotalCCusto(rs);
				}
			});
		} catch (Exception e) {
			mbioLogger.error("Consulta Rateio Total por Centro de Custo ", e);
			return new ArrayList<>();
		}
	}
	
	 private ResultadoPesquisaTotalRateio retornarConsultaTotalCCusto(ResultSet rs) throws SQLException {
	        int rowNum = 1;

	        int externo = rs.getInt(rowNum++);
	        int reuso = rs.getInt(rowNum++); 
	        int crivo = rs.getInt(rowNum++); 

	        return new ResultadoPesquisaTotalRateio(externo, reuso, crivo);
	 }
	 
	 /** Pesquisa Consolidada por Bureau */
	@Override
	public List<ResultadoPesquisaCBureau> consultaCBureau(FiltroPesquisaConsultasRateio parametros) {
		Object[] param = { };
		String query = "";
		ServicoEnum[] servicos = ServicoEnum.values();
		for (ServicoEnum servicoEnum : servicos) {
			if(!"-1".equals(parametros.getBureau())){
				if (servicoEnum.getBureau().equals(parametros.getBureau())) {
					query = SQL_SELECT_CONSOLIDADA_BUREAU;
					if("1".equals(parametros.getAcessoExterno())){
						query += " AND CI.COD_TPO_CONS <> 2";
					}
					if("2".equals(parametros.getAcessoExterno())){
						query += " AND CI.COD_TPO_CONS = 2";
					}
					if(!"-1".equals(parametros.getServico())){
						query += " AND SBI.NOM_INTGC = '"+ parametros.getServico() +"'";
					}else{
						query += " AND SBI.NOM_INTGC = '"+ servicoEnum.name() +"'";
					}
					query += " GROUP BY CC.DES_CNTRO_CUSTO, SBI.NOM_INTGC, SBI.NOM_SERVC, SBI.COD_ORIGE_INFO, CI.COD_TPO_CONS";
					query += " ORDER BY CC.DES_CNTRO_CUSTO, SBI.NOM_INTGC, SBI.NOM_SERVC, SBI.COD_ORIGE_INFO, CI.COD_TPO_CONS";
				}
			}else{
				query = SQL_SELECT_CONSOLIDADA_BUREAU;
				if("1".equals(parametros.getAcessoExterno())){
					query += " AND CI.COD_TPO_CONS <> 2";
				}
				if("2".equals(parametros.getAcessoExterno())){
					query += " AND CI.COD_TPO_CONS = 2";
				}
				query += " GROUP BY CC.DES_CNTRO_CUSTO, SBI.NOM_INTGC, SBI.NOM_SERVC, SBI.COD_ORIGE_INFO, CI.COD_TPO_CONS";
				query += " ORDER BY CC.DES_CNTRO_CUSTO, SBI.NOM_INTGC, SBI.NOM_SERVC, SBI.COD_ORIGE_INFO, CI.COD_TPO_CONS";
			}
			
			if(!query.equals("")){
				try {
					return jdbcTemplate.query(query, param, new RowMapper<ResultadoPesquisaCBureau>() {
						@Override
						public ResultadoPesquisaCBureau mapRow(ResultSet rs, int rowNum) throws SQLException {
							return retornaConsultaRateioCBureau(rs);
						}
					});
				} catch (Exception e) {
					mbioLogger.error("Consulta Rateio Consolidada por Bureau", e);
					return new ArrayList<>();
				}
			}
		}
		return null;
	}
	
	private ResultadoPesquisaCBureau retornaConsultaRateioCBureau(ResultSet rs) throws SQLException{
		 int rowNum = 1;
		 
		 String centroCusto = rs.getString(rowNum++);
		 String nomeIntegracao = rs.getString(rowNum++);
		 String nomeServ = rs.getString(rowNum++);
		 int codOrigemInfo = rs.getInt(rowNum++);
		 int codTipoConsulta = rs.getInt(rowNum++);
		 int qtdBureau = rs.getInt(rowNum++);
		 int qdtReuso = rs.getInt(rowNum++);
		 int qtdCrivo = rs.getInt(rowNum++);
		
		return new ResultadoPesquisaCBureau(centroCusto, nomeIntegracao, null, nomeServ, codOrigemInfo, qtdBureau, qdtReuso, qtdCrivo, codTipoConsulta);
	}

	@Override
	public List<ResultadoGraficoRateio> consultaGrafico(FiltroPesquisaConsultasRateio parametros) {
		Object[] parameters = null;
		
		String query = SQL_SELECT_GRAFICO;
		
		try {
            return jdbcTemplate.query(query, parameters, new RowMapper<ResultadoGraficoRateio>() {
                @Override
                public ResultadoGraficoRateio mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return retornaConsultaGrafico(rs);
                }
            });
        } catch (Exception e) {
            mbioLogger.error("Pesquisa de Consulta Gráfico Consumo Mensal por Bureau/Serviço", e);
            return new ArrayList<>();
        }
		
	}
	private ResultadoGraficoRateio retornaConsultaGrafico(ResultSet rs) throws SQLException {
        int rowNum = 1;
        
        String centroCusto = rs.getString(rowNum++);
        String nomeIntegracao = rs.getString(rowNum++);
        String servico = rs.getString(rowNum++);
        String mesAnoAcesso = rs.getString(rowNum++);
        int qtdAcessos = rs.getInt(rowNum++);

        return new ResultadoGraficoRateio(centroCusto, nomeIntegracao, servico, mesAnoAcesso, qtdAcessos);
    }
	

}