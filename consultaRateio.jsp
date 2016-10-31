<%@ page language='java' contentType='text/html; charset=UTF-8'
	pageEncoding='UTF-8'%>
<%@ include file='/jsp/imports/imports.jsp'%>
<!DOCTYPE html>
<html>
<mbio:content title="Rateio de Consultas" >
	<form id="formPesquisa">

		<!-- CONSULTA RATEIO -->
		<div id="periodos" class="row">
			<div class="col-md-2">
				<mbio:paragrafo span="Período*">
					<input id="dtIni" name="periodoInicio" type="text"  class="dataInput form-control" value="${periodoInicio}" onchange="validaMesAno()"  onblur="inputDate.validate('#dtIni')" required="true" >
				</mbio:paragrafo>
			</div>
			<div class="col-sm-1 padding-input" style="margin-left: -1.5%;">
				<mbio:paragrafo>
					<label>à</label>
				</mbio:paragrafo>
			</div>
			<div class="col-md-2 padding-input" style="margin-left: -7.5%;">
				<mbio:paragrafo>
					<input id="dtFim" name="periodoFinal" type="text" class="dataInput form-control" value="${periodoFinal}" onchange="validaMesAno()" onblur="inputDate.validate('#dtFim')" maxlength="10" required="true">
				</mbio:paragrafo>
			</div>

			<div class="col-sm-1 padding-input" style="margin-left: -1.0%;">
				<mbio:paragrafo>
					<label>ou</label>
				</mbio:paragrafo>
			</div>

			<div class="col-md-2" style="margin-left: -6.5%;">
				<mbio:paragrafo span="Mês*">
					<select class="form-control" id="mes" name="mes" onchange="validaPeriodo()" required="true">
						<option value="-1">Selecione ...</option>
						<c:forEach items="${meses}" var="item">
							<option value="${item.value}">${item.descricao}</option>
						</c:forEach>
					</select>
				</mbio:paragrafo>
			</div>

			<div class="col-md-2" style="margin-left: -0.3%;">
				<mbio:paragrafo span="Ano*">
					<select class="form-control" id="ano" name="ano" onchange="validaPeriodo()" required="true">
						<option value="-1">Selecione ...</option>
						<c:forEach items="${anos}" var="item">
							<option value="${item}">${item}</option>
						</c:forEach>
					</select>
				</mbio:paragrafo>
			</div>



		</div>

		<div id="labelTipoConsulta" class="row">
			<div class="col-md-8">
				<mbio:paragrafo span="Tipo Consulta">
					<label></label>
				</mbio:paragrafo>
				
				<mbio:paragrafo>
					<div class="row" style="margin-left: 0.5%;">
						<input id="nomeConsulta" name="nomeConsulta" type="radio" onclick="ativaRadio(this.value)" value="consultaAnalitica"> Analítica 
						<input id="nomeConsulta" name="nomeConsulta" type="radio" onclick="ativaRadio(this.value)" value="consolidadaCCusto" style="margin-left: 1.5%;"> Consolidada por Centro de	Custo 
						<input id="nomeConsulta" name="nomeConsulta" type="radio" onclick="ativaRadio(this.value)" value="consolidadaBureau" style="margin-left: 1.5%;"> Consolidada por Bureau 
						<input id="nomeConsulta" name="nomeConsulta" type="radio" onclick="ativaRadio(this.value)" value="grafico" style="margin-left: 1.5%;"> Gráfico Consumo Mensal 
						<input id="tipoConsulta" name="tipoConsulta" type="hidden" value="">
					</div>
				</mbio:paragrafo>
			</div>
			
		</div>

		<div id="consultaR" class="row">

			<div class="col-md-4">

				<mbio:paragrafo span="Centro de Custo">
					<select id="centroCusto" name="centroCusto" class="form-control">
						<mbio:selectedOption items="${centrosCusto}"
							selectedItem="${centroCusto}" />
					</select>
				</mbio:paragrafo>
			</div>
			<div class="col-md-4">
				<mbio:paragrafo span="Usuário">
					<input id="user" name="usuario" type="text" class="form-control" value="${usuario}" maxlength="50">
				</mbio:paragrafo>
			</div>
		</div>

		<div class="row">
			<div class="col-md-4">
				<mbio:paragrafo span="Bureau">
					<select id="bureau" name="bureau" class="form-control">
						<option value="-1" selected>Selecione...</option>
						<%-- <c:if test="${bureauSelecionado ne '-1'}">
							<option selected>${bureauSelecionado}</option>
							<c:forEach items="${bureau}" var="item">
								<c:if test="${not (item.bureau eq bureauSelecionado)}">
									<option value="${item.bureau}">${item.bureau}</option>
								</c:if>
							</c:forEach>
						</c:if> --%>
						<c:if test="${bureauSelecionado eq '-1'}">
							<c:forEach items="${bureau}" var="item">
								<option value="${item.bureau}">${item.bureau}</option>
							</c:forEach>
						</c:if>
					</select>
				</mbio:paragrafo>
			</div>

			<div class="col-md-4">
				<p>
				<span>Serviço</span>
				<select id="servico" name="servico" class="form-control">
					<option value="-1" selected>Selecione...</option>
					<c:if test="${servicoSelecionado ne '-1'}">
						<option selected>${servicoSelecionado}</option>
						<c:forEach items="${servicos}" var="item">
							<c:if test="${not (item.servico eq servicoSelecionado)}">
								<option value="${item.servico}">${item.servico}</option>
							</c:if>
						</c:forEach>
					</c:if>
					<c:if test="${servicoSelecionado eq '-1'}">
						<c:forEach items="${servicos}" var="item">
							<option value="${item.servico}">${item.servico}</option>
						</c:forEach>	
					</c:if>
				</select>
					
				<p>
			</div>
		</div>

		<div class="row">
			<div class="col-md-4">
				<p>
					<span>Fonte de Informação</span>
					<select id="fonteInformacao" name="fonteInformacao" class="form-control">
						<option value="-1" selected>Selecione...</option>
						<c:if test="${fonteInformacaoSelecionado ne '-1'}">
							<option selected>${fonteInformacaoSelecionado}</option>

							<c:forEach items="${fonteInformacaoSelecionado}" var="item">
								<c:if test="${not (item eq fonteInformacaoSelecionado)}">
									<option value="${item.valor}">${item.descricao}</option>
								</c:if>
							</c:forEach>
							
						</c:if>
						<c:if test="${fonteInformacaoSelecionado eq '-1'}">
							<c:forEach items="${fonteInformacoes}" var="item">
								<option value="${item.valor}">${item.descricao}</option>
							</c:forEach>	
						</c:if>
					</select>
				<p>
			</div>


			<div class="col-md-4">
				<mbio:paragrafo span="Acesso Externo">			   
					<select id="acessoExterno" name="acessoExterno" class="form-control">
						<option value="-1" selected>Selecione...</option>
						<c:if test="${acessoExternoSelecionado ne '-1'}">
							<option selected>${acessoExternoSelecionado}</option>
							<c:forEach items="${acessoExterno}" var="item">
								<c:if test="${not (item eq acessoExternoSelecionado)}">
									<option value="${item.value}">${item.texto}</option>
								</c:if>
							</c:forEach>
						</c:if>
						<c:if test="${acessoExternoSelecionado eq '-1'}">
							<c:forEach items="${acessoExterno}" var="item">
								<option value="${item.value}">${item.texto}</option>
							</c:forEach>	
						</c:if>
					</select>
					
				</mbio:paragrafo> 
			</div>

			<div class="col-md-2">
				<button id="consultar" class="btn btn-success btn-middle col-md-12">Consultar</button>
				<input id="consultarHidden" name="consultar" type="hidden"
					value="${consultar}">
			</div>
		</div>
	</form>


	<!-- RESULTADOS NAO ENCONTRADOS -->
	<div id="tbRegistros">
		<div class="title-bar">Resultados</div>
		<c:if test="${resultados == null || resultados.size() == 0}">
			<div class="text-center col-xs-12 col-md-12" id="nenhumRegistro">
				<label>Nenhum registro encontrado.</label>
			</div>
		</c:if>
	</div>
	

	<!-- RESULTADOS CONSULTA ANALITICA -->
	<div id="tbAnalitica">
		<div class="title-bar">Resultados</div>
		<mbio:table pagination="true">
			<thead>
				<tr>
					<th>Data</th>
					<th>Centro de Custo</th>
					<th>Usuário</th>
					<th>Fonte Informação</th>
					<th>Bureau</th>
					<th>Serviço</th>
					<th>Reuso</th>
					<th>CPF-CNPJ</th>
				</tr>
			</thead>
			<tbody>
				
			</tbody>
		</mbio:table>
		<div class="col-md-offset-5">
			<button class="btn btn-success btn-middle col-md-2">Exportar CSV</button>		
			<button class="btn btn-success btn-middle col-md-2">Gerar PDF</button>
		</div>
	</div>

	<!-- Consulta Consolidade Centro de Custo -->	
	<div id="tbCentroCusto">
		<div class="title-bar">Resultados</div>
		<mbio:table pagination="true">
			<thead>
				<tr>
					<th>Centro de Custo</th>
					<th>Bureau</th>
					<th>Serviço</th>
					<th>Qtd Acessos Bureau</th>
					<th>Qtd Acessos Reuso</th>
					<th>Qtd Acessos Crivo</th>
				</tr>
			</thead>
			<tbody>
				
			</tbody>
		</mbio:table>
		
		<div class="col-md-offset-5">
			<button class="btn btn-success btn-middle col-md-2">Exportar CSV</button>		
			<button class="btn btn-success btn-middle col-md-2">Gerar PDF</button>
		</div>

	</div> 
	
	<div id="tbCentroCustoBureau">
		<div class="title-bar">Resultados</div>
		<mbio:table pagination="true">
				<thead>
					<tr>
						<th>Bureau</th>
						<th>Serviço</th>
						<th>Qtd Acessos Bureau</th>
						<th>Qtd Acessos Reuso</th>
						<th>Qtd Acessos Crivo</th>
					</tr>
				</thead>
			<tbody>
				
			</tbody>
		</mbio:table>
		
		<div class="col-md-offset-5">
			<button class="btn btn-success btn-middle col-md-2">Exportar CSV</button>		
			<button class="btn btn-success btn-middle col-md-2">Gerar PDF</button>
		</div>

	</div> 
	
	<div id="tbConsumoMensalBureau">
		<div class="title-bar">Resultados</div>
		<div id="consumoMensalBureau" style="width: 90%; height: 300px;display: inline-block;">
	</div>
	
	

	<!-- POPUP MESSAGE OK-->
	<mbio:dialog />
</mbio:content>
<script src='<c:url value="/js/pages/consultaRateio.js" />'></script>
<script src='<c:url value="/js/plugins/jquery.mask.input.js" />'></script>
<script src='<c:url value="/js/inputDate.js" />'></script>
<script src='<c:url value="/js/plugins/canvasjs.min.js" />'></script>
