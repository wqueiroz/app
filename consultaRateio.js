var consulta = consulta || {};

var $form = $('#formPesquisa');

var $divPeriodos = $('#periodos');
var $divConsulta = $('#consultaR');
var consultaDefault = $('input:radio[name=nomeConsulta][value=analitica]').val();

var $btnPesquisar = $('#consultar');
$myRadio = $( '#nomeConsulta' );

var $selectFonteInformacao = $('#fonteInformacao');
var $selectBureau = $('#bureau');
var $selectServico = $('#servico');
var $selectCentroCusto = $('#centroCusto');

var $radioSim = $('#sim');
var $radioNao = $('#nao');


var ERR0001 = "Campos obrigatórios não informados.";

$(document).ready(function() {
	util.removerCaracteresEspeciais(":text[id!='dtIni'][id!='dtFim']");
	$('#tbAnalitica').hide();
	$('#tbCentroCusto').hide();
	$('#tbCentroCustoBureau').hide();
	
	$myRadio[0].click();
	if ($selectBureau.val() == -1) {
		consulta.servicos();
	}

});

function validarPesquisa() {
	var periodosVazios = ($('#dtIni').val() != '' && $('#dtFim').val() != '');
	var mesAnoVazios = ($('#mes').val() != -1 && $('#ano').val() != -1);
	
	var retorno = true;
	
	if (!periodosVazios && !mesAnoVazios) {
		util.mostrarMensagem(ERR0001);
		return false;
	} else {
		if (periodosVazios){
			var startDate = toDate($('#dtIni').val());
			var endDate = toDate($('#dtFim').val());

			if (startDate > endDate) {
				util.mostrarMensagem('Data Final deve ser igual ou posterior a Data InÃ­cio.');
				return false;
			}
		}
	}
	return retorno;
}

function validaMesAno(){
	if($('#dtIni').val() != '' || $('#dtFim').val() != ''){
		$('#mes').prop('disabled', true);
		$('#ano').prop('disabled', true);
	}else{
		$('#mes').prop('disabled', false);
		$('#ano').prop('disabled', false);
	}
}

function validaPeriodo(){
	if($('#mes').val() != -1 || $('#ano').val() != -1){
		$('#dtIni').prop('disabled', true);
		$('#dtFim').prop('disabled', true);
	}else{
		$('#dtIni').prop('disabled', false);
		$('#dtFim').prop('disabled', false);
	}
}

$(function() {
	$btnPesquisar.on('click', function(e) {
		e.preventDefault();
		
		//limpa dados de todas as tabelas para receber Nov/16a consulta
		limpaTabelas();
		
		
		var consulta = $("#tipoConsulta").val();
		if (validarPesquisa()) {
			var dados = $form.serialize();
			var consultar = {
				url : util.path(consulta),
				type : 'GET',
				data : dados,
				success : function(retorno) {
					console.log(retorno);
					
					switch (consulta) {
					case 'consultaAnalitica':
						retornaConsultaAnaliticca(retorno);
						break;
						
					case 'consolidadaCCusto':
						retornaConsultaCCusto(retorno);
						break;
						
					case 'consolidadaBureau':
						retornaConsultaCBureau(retorno);
						break;
						
					case 'grafico':
						montaGrafico(retorno);
						break;
						
					default:
						break;
					}
				},
				error : function(erro){
					console.log("error  " + erro);
				}
			}
			util.loading('#consultar');
			$.ajax(consultar);
		}
	});
});


function limpaTabelas(){
	$('#tbAnalitica tbody > tr').remove();
	$('#tbCentroCusto tbody > tr').remove();
	$('#tbCentroCustoBureau tbody > tr').remove();
	
	$('#tbAnalitica').hide();
	$('#tbCentroCusto').hide();
	$('#tbCentroCustoBureau').hide();
}

var ativaRadio = function(v) {
		$("#tipoConsulta").val(v);
}


function toDate(date) {
	var parts = date.split("/");
	return new Date(parts[2], parts[1] - 1, parts[0]);
}


function radioValue() {
	if ($radioSim.is(':checked')) {
		return 1;
	} else {
		return 0;
	}
}


function limparCampos() {
	
	$(':text').val("");
	$(':radio').attr('checked', false);
	$('select').val(-1);
	
	// limpar table
	$('table').remove();
	$('#nenhumRegistro').remove();
	$('#paginacao').remove();
	$('.title-bar').after('	<div class="text-center col-xs-12 col-md-12" id="nenhumRegistro"> \
								<label>Nenhum registro encontrado.</label> \
							</div> ')
	
	util.enableComponent('#origem');
}


// carrega combo SERVIÇOS com todas opções
consulta.servicos = function() {
	var bureau = $selectBureau.val();

	$.getJSON(util.path("selecionaBureau"), {
		bureau : bureau
	}).done(function(response) {
		preencherComboServico(response);
	});
	
}

function preencherComboServico(response) {
	var options = '<option value="-1" selected>Selecione...</option>';
	$('#servico option').remove();
	$.each(response, function(index, item) {
		options += '<option value="' + item + '">' + item + '</option>';
	});
	$selectServico.html(options);
}

function preencherComboInformacao(response) {
	var options = '<option value="-1" selected>Selecione...</option>';
	$('#fonteInformacao option').remove();
	$.each(response, function(index, item) {
		if (item == 'DIRETA') {
			options += '<option value="' + '0' + '">' + 'Direta' + '</option>';
		}else{
			options += '<option value="' + '1' + '">' + 'Crivo' + '</option>';
		}
		
	});
	$selectFonteInformacao.html(options);
}

//carrega combo SERVIÇOS coforme opção da combo BUREAU selecionada
$selectBureau.change(function() {	
	
	var bureau = $selectBureau.val();

	$.getJSON(util.path("selecionaBureau"), {
		bureau : bureau
	}).done(function(response) {
		preencherComboServico(response);
	});
});


//carrega combo FONTE INFORMAÇÃO coforme opção da combo SERVIÇO selecionada
$selectServico.change(function() {	
	
	var servico = $selectServico.val();

	$.getJSON(util.path("selecionaServico"), {
		servico : servico
	}).done(function(response) {
		preencherComboInformacao(response);
	});
});

function loadReady(){
	$('#overlay').remove();
	$('#consultar').prop('disabled', false);
	$('#consultar').remove(".fa-refresh .fa-spin");
	$('#primeiro').click();
	$('#consultar').html("Consultar");
}

function retornaConsultaAnaliticca(retorno){
	$('#tbCentroCustoBureau table').prop('id','tbCentroCustoBureau');
	$('#tbCentroCusto table').prop('id','tbCentroCusto');
	$('#tbAnalitica table').prop('id','tbAnalitica');
	
	var lista = retorno.listaReultados;
	
	if (lista == null || lista.length == 0) {
		$('#tbRegistros').show();
	}else{
		$('#tbRegistros').hide();
		$('#tbAnalitica').show();
		
		for(var i = 0; i < lista.length ; i++){
			var linha = '<tr>';
			linha += '<td>' + lista[i].data + '</td>';
			linha += '<td>' + lista[i].centroCusto + '</td>';
			linha += '<td>' + lista[i].usuario + '</td>';
			linha += '<td>' + lista[i].fonteInfo + '</td>';
			linha += '<td>' + lista[i].bureau + '</td>';
			linha += '<td>' + lista[i].servico + '</td>';
			linha += '<td>' + lista[i].reuso + '</td>';
			linha += '<td>' + lista[i].cpfCnpj + '</td>';
			linha += '</tr>';
			$('#tbAnalitica tbody:last-child').append(linha);
		}
		carregaPaginacao(10, 'tbAnalitica');
		$('div#tbAnalitica #paginacao').each(function(index, element) {
			$(element).click(function() {
				$('#hiddenPage').val('S');
			});
		});
	}
	loadReady();
}

function retornaConsultaCCusto(retorno){
	$('#tbCentroCustoBureau table').prop('id','tbCentroCustoBureau');
	$('#tbCentroCusto table').prop('id','tbCentroCusto');
	$('#tbAnalitica table').prop('id','tbAnalitica');
	
	var lista = retorno.listaCCusto;
	
	if (lista == null || lista.length == 0) {
		$('#tbRegistros').show();
	}else{
		$('#tbRegistros').hide();
		$('#tbCentroCusto').show();
		
		var lista = retorno.listaCCusto;
		for(var i = 0; i < lista.length; i++){
			var linha = '<tr>';
			linha += '<td>' + verificaNull(lista[i].centroCusto) + '</td>';
			linha += '<td>' + verificaNull(lista[i].bureau) + '</td>';
			linha += '<td>' + verificaNull(lista[i].servico) + '</td>';
			linha += '<td>' + verificaNull(lista[i].externo) + '</td>';
			linha += '<td>' + verificaNull(lista[i].reuso) + '</td>';
			linha += '<td>' + verificaNull(lista[i].crivo) + '</td>';
			linha += '</tr>';
			$('#tbCentroCusto tbody:last-child').append(linha);
		}
		carregaPaginacao(10, 'tbCentroCusto');
		$('div#tbCentroCusto #paginacao').each(function(index, element) {
			$(element).click(function() {
				$('#hiddenPage').val('S');
			});
		});
	}
	loadReady();
}

function verificaNull(texto){
	if (texto == null){
		return texto = "";
	}
	return texto;
}

function retornaConsultaCBureau(retorno){
	$('#tbCentroCustoBureau table').prop('id','tbCentroCustoBureau');
	$('#tbCentroCusto table').prop('id','tbCentroCusto');
	$('#tbAnalitica table').prop('id','tbAnalitica');
	
	if (lista == null || lista.length == 0) {
		$('#tbRegistros').show();
	}else{
		$('#tbRegistros').hide();
		$('#tbCentroCustoBureau').show();
		
		var lista = retorno.listaCBureau;
		for(var i = 0; i < lista.length; i++){
			var linha = '<tr>';
			linha += '<td>' + lista[i].bureau + '</td>';
			linha += '<td>' + lista[i].servico + '</td>';
			linha += '<td>' + lista[i].qtdAcessoBureau + '</td>';
			linha += '<td>' + lista[i].qtdAcessoReuso + '</td>';
			linha += '<td>' + lista[i].qtdAcessoCrivo + '</td>';
			linha += '</tr>';
			$('#tbCentroCustoBureau tbody:last-child').append(linha);
		}
		carregaPaginacao(10, 'tbCentroCustoBureau');
		$('#paginacao').each(function(index, element) {
			$(element).click(function() {
				$('#hiddenPage').val('S');
			});
		});
	}
	loadReady();
}


function montaGrafico(retorno){
	var lista = retorno.listaGrafico;
	
	if (lista == null || lista.length == 0) {
		$('#tbRegistros').show();
	}else{
		$('#tbRegistros').hide();
		$('#tbConsumoMensalBureau').show();
		
		var chart = new CanvasJS.Chart("consumoMensalBureau",
		{
			title:{
				text: "Consumo Mensal por Bureau/Serviço"
			},   
                        animationEnabled: true,  
			axisY:{ 
				title: "Quantidade Consultas",
				include: false,
				titleFontColor: "black"
			},
			axisX: {
				title: "Mês/Ano",
				interval: 1,
				titleFontColor: "black"
			},
			toolTip: {
				shared: true,
				content: function(e){
					var body = new String;
					var head ;
					for (var i = 0; i < e.entries.length; i++){
						var  str = "<span style= 'color:"+e.entries[i].dataSeries.color + "'> " + e.entries[i].dataSeries.name + "</span>: <strong>"+  e.entries[i].dataPoint.y + "</strong>'' <br/>" ; 
						body = body.concat(str);
					}
					head = "<span style = 'color:DodgerBlue; '><strong>"+ (e.entries[0].dataPoint.label) + "</strong></span><br/>";

					return (head.concat(body));
				}
			},
			legend: {
				horizontalAlign: "right",
				verticalAlign: "center"
			},
			data: [
			{        
				type: "line",
				showInLegend: true,
				name: "Bacen - SRC",
				dataPoints: [
				{label: "Jan/16" , y: 3.92} ,     
				{label: "Fev/16" , y: 3.31} ,     
				{label: "Mar/16" , y: 3.85} ,     
				{label: "Abr/16" , y: 3.60} ,     
				{label: "Mai/16" , y: 3.24} ,     
				{label: "Jun/16" , y: 3.22} ,     
				{label: "Jul/16" , y: 3.06} ,     
				{label: "Ago/16" , y: 3.37} ,     
				{label: "Set/16" , y: 3.47} ,     
				{label: "Out/16" , y: 3.79} ,     
				{label: "Nov/16" , y: 3.98} ,     
				{label: "Dez/16" , y: 3.73} 
				]
			},
			{        
				type: "line",
				showInLegend: true,
				name: "Boa Vista - Pessoal Gold",
				dataPoints: [
				{label: "Jan" , y: 2.98} ,     
				{label: "Fev/16" , y: 3.11} ,     
				{label: "Mar/16" , y: 2.4} ,     
				{label: "Abr/16" , y: 0.63} ,     
				{label: "Mai/16" , y: 0.24} ,     
				{label: "Jun/16" , y: 0.08} ,     
				{label: "Jul/16" , y: 0.03} ,     
				{label: "Ago/16" , y: 0.14} ,     
				{label: "Set/16" , y: 0.26} ,     
				{label: "Out/16" , y: 0.36} ,     
				{label: "Nov/16" , y: 1.13} ,     
				{label: "Dez/16" , y: 1.79}
				]
			}, 
			{        
				type: "line",
				showInLegend: true,
				name: "Boa Vista - Score",
				dataPoints: [
				{label: "Jan" , y: 3.16} ,     
				{label: "Fev/16" , y: 2.42} ,     
				{label: "Mar/16" , y: 2.99} ,     
				{label: "Abr/16" , y: 3.04} ,     
				{label: "Mai/16" , y: 3.35} ,     
				{label: "Jun/16" , y: 3.82} ,     
				{label: "Jul/16" , y: 3.14} ,     
				{label: "Ago/16" , y: 3.87} ,     
				{label: "Set/16" , y: 3.84} ,     
				{label: "Out/16" , y: 3.19} ,     
				{label: "Nov/16" , y: 3.92} ,     
				{label: "Dez/16" , y: 3.8}    
				]
			}, 
			{        
				type: "line",
				showInLegend: true,
				name: "Receita Federal - Infoconv-WS PF",
				dataPoints: [
				{label: "Jan" , y: 5.24} ,     
				{label: "Fev/16" , y: 4.09} ,     
				{label: "Mar/16" , y: 3.92} ,     
				{label: "Abr/16" , y: 2.75} ,     
				{label: "Mai/16" , y: 2.03} ,     
				{label: "Jun/16" , y: 1.55} ,     
				{label: "Jul/16" , y: 0.93} ,     
				{label: "Ago/16" , y: 1.16} ,     
				{label: "Set/16" , y: 1.61} ,     
				{label: "Out/16" , y: 3.24} ,     
				{label: "Nov/16" , y: 5.67} ,     
				{label: "Dez/16" , y: 6.06}    
				]
			}, 
			{        
				type: "line",
				showInLegend: true,
				name: "Serasa - Concentre",
				dataPoints: [
				{label: "Jan" , y: 4.72} ,     
				{label: "Fev/16" , y: 4.15} ,     
				{label: "Mar/16" , y: 3.4} ,     
				{label: "Abr/16" , y: 1.25} ,     
				{label: "Mai/16" , y: .54} ,     
				{label: "Jun/16" , y: 0.13} ,     
				{label: "Jul/16" , y: 0.04} ,     
				{label: "Ago/16" , y: 0.09} ,     
				{label: "Set/16" , y: 0.28} ,     
				{label: "Out/16" , y: 1.19} ,     
				{label: "Nov/16" , y: 3.31} ,     
				{label: "Dez/16" , y: 3.18}  
				]
			}, 
			{        
				type: "line",
				showInLegend: true,
				name: "Serasa - Credit Bureau Analítico",
				dataPoints: [
				{label: "Jan" , y: 4.72} ,     
				{label: "Fev/16" , y: 4.15} ,     
				{label: "Mar/16" , y: 3.4} ,     
				{label: "Abr/16" , y: 1.25} ,     
				{label: "Mai/16" , y: .54} ,     
				{label: "Jun/16" , y: 0.13} ,     
				{label: "Jul/16" , y: 0.04} ,     
				{label: "Ago/16" , y: 0.09} ,     
				{label: "Set/16" , y: 0.28} ,     
				{label: "Out/16" , y: 1.19} ,     
				{label: "Nov/16" , y: 3.31} ,     
				{label: "Dez/16" , y: 3.18}   
				]
			}
			],
          legend :{
	    	horizontalAlign: "right",
			verticalAlign: "center",
            cursor:"pointer",
            itemclick : function(e) {
              if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
				e.dataSeries.visible = false;
              }
              else{
				e.dataSeries.visible = true;
              }
            }
          }
          
		});

		chart.render();
	}
	
	loadReady();
}

