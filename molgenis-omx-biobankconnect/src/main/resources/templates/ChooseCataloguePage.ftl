<script src="/js/jquery.bootstrap.pager.js"></script>
<script src="/js/bootstrap-fileupload.min.js"></script>
<script src="/js/common-component.js"></script>
<script src="/js/biobank-connect.js"></script>
<script src="/js/catalogue-chooser.js"></script>
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.9.2.custom.min.css">
<link rel="stylesheet" type="text/css" href="/css/bootstrap-fileupload.min.css">
<link rel="stylesheet" type="text/css" href="/css/biobank-connect.css">
<link rel="stylesheet" type="text/css" href="/css/catalogue-chooser.css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div id="div-info" class="span12 well custom-white-well">	
					<br>
					<div class="row-fluid">
						<div class="span9"><legend class="legend">
							Browse catalogue : 
							<select id="selectedDataSetId" name="selectedDataSetId">
								<#if wizard.selectedDataSet??>
									<#list wizard.dataSets as dataset>
										<option value="${dataset.id?c}"<#if dataset.id?c == wizard.selectedDataSet.id?c> selected</#if>>${dataset.name}</option>
									</#list>
								<#else>
									<#list wizard.dataSets as dataset>
										<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
									</#list>
								</#if>
							</select>
							<button id="import-data-button" class="btn btn-primary" type="btn">Import data</button>
						</div>
						<div  id="div-search" class="span3">
							<div><strong>Search data items :</strong></div>
							<div class="input-append">
								<input id="search-dataitem" type="text" title="Enter your search term" />
								<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span4">
							Number of data items : <span id="dataitem-number"></span>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span12">
							<div id="container" class="data-table-container"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="row-fluid">
		<div id="import-features-modal" class="modal hide">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h3>Import features</h3>
			</div>
			<div class="modal-body">
				<div><strong>Please input dataSet name</strong></div>
				<div><input type="text" id="dataSetName" name="dataSetName"/><span class="float-right">E.g. LifeLines</span></div><br>
				<div><strong>Please upload features</strong></div>
				<div>
					<div class="fileupload fileupload-new" data-provides="fileupload">
						<div class="input-append">
							<div class="uneditable-input">
								<i class="icon-file fileupload-exists"></i>
								<span class="fileupload-preview"></span>
							</div>
							<span class="btn btn-file btn-info">
								<span class="fileupload-new">Select file</span>
								<span class="fileupload-exists">Change</span>
								<input type="file" id="uploadedOntology" name="file" required/>
							</span>
							<a href="#" class="btn btn-danger fileupload-exists" data-dismiss="fileupload">Remove</a>
						</div>
						<div class="float-right"><a href="/html/example-data.csv">Download example data</a></div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal">Close</a>
				<a href="#" class="btn btn-primary" id="import-features">Import features</a>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(function(){
			var molgenis = window.top.molgenis;
			molgenis.ontologyMatcherRunning(function(){
				var catalogueChooser = new molgenis.CatalogueChooser();
				catalogueChooser.changeDataSet($('#selectedDataSetId').val());
				
				$('#selectedDataSetId').change(function(){
					catalogueChooser.changeDataSet($('#selectedDataSetId').val());
				});
				
				if($('#selectedDataSetId option').length === 0){
					$('.pager li.next').addClass('disabled');
				}
				
				$('#import-data-button').click(function(){
					$('#import-features-modal').modal('show');
					return false;
				});
				
				$('#import-features').click(function(){
					var alert = {};
					var uploadedFile = $('#uploadedOntology').val();
					if($('#dataSetName').val() === ''){
						alert.message = 'Please define the dataset name!';
						molgenis.createAlert([alert], 'error');
						$('#import-features-modal').modal('hide');
					}else if(uploadedFile === '' || uploadedFile.substr(uploadedFile.length - 4, uploadedFile.length) !== '.csv'){
						alert.message = 'Please upload your file in CSV';
						molgenis.createAlert([alert], 'error');
						$('#import-features-modal').modal('hide');
					}else{
						$('#wizardForm').attr({
							'action' : '${context_url}/uploadfeatures',
							'method' : 'POST'
						}).submit();
					}
				});
				<#if message??>
					var alert = {};
					alert.message = '${message}';
					molgenis.createAlert([alert], 'error');
				</#if>
			});
		});
	</script>
</form>