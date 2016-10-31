package br.com.original.mbio.controlador.dominio;

public class ResultadoGraficoRateio {
	
	private String centroCusto;
	private String nomeIntegracao;
	private String servico;
	private String mesAnoAcesso;
	private int qtdAcessos;
	
	
	public ResultadoGraficoRateio(String centroCusto, String nomeIntegracao, String servico,String mesAnoAcesso, int qtdAcessos) {
		this.centroCusto = centroCusto;
		this.nomeIntegracao = nomeIntegracao;
		this.servico = servico;
		this.mesAnoAcesso = mesAnoAcesso;
		this.qtdAcessos = qtdAcessos;
	}


	public String getCentroCusto() {
		return centroCusto;
	}


	public void setCentroCusto(String centroCusto) {
		this.centroCusto = centroCusto;
	}


	public String getNomeIntegracao() {
		return nomeIntegracao;
	}


	public void setNomeIntegracao(String nomeIntegracao) {
		this.nomeIntegracao = nomeIntegracao;
	}


	public String getServico() {
		return servico;
	}


	public void setServico(String servico) {
		this.servico = servico;
	}


	public String getMesAnoAcesso() {
		return mesAnoAcesso;
	}


	public void setMesAnoAcesso(String mesAnoAcesso) {
		this.mesAnoAcesso = mesAnoAcesso;
	}


	public int getQtdAcessos() {
		return qtdAcessos;
	}


	public void setQtdAcessos(int qtdAcessos) {
		this.qtdAcessos = qtdAcessos;
	}


	
	
}
