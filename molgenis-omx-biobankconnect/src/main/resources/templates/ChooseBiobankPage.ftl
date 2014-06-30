<script src="/js/jquery-ui-1.9.2.custom.min.js"></script>
<script src="/js/jquery.bootstrap.pager.js"></script>
<script src="/js/bootstrap-fileupload.min.js"></script>
<script src="/js/common-component.js"></script>
<script src="/js/biobank-connect.js"></script>
<script src="/js/ontology-matcher.js"></script>
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.9.2.custom.min.css">
<link rel="stylesheet" type="text/css" href="/css/biobank-connect.css">
<link rel="stylesheet" type="text/css" href="/css/ontology-matcher.css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="offset3 span6 div-inputinfo well">
					<div class="row-fluid">
						<div class="span12">
							<legend class="ontology-matcher-legend">
								Select matching catalogues for : <strong>${wizard.selectedDataSet.name}</strong>
							</legend>
							<div class="row-fluid">
								<div class="span4">
									<select id="targetDataSets" name="targetDataSets">
										<#list wizard.dataSets as dataset>
											<#if dataset.id?c != wizard.selectedDataSet.id?c>
												<option value="${dataset.id?c}"<#if dataset.id?c == wizard.selectedDataSet.id?c> selected</#if>>${dataset.name}</option>
											</#if>
										</#list>
									</select>
									<input type="hidden" id="selectedTargetDataSets" name="selectedTargetDataSets">
								</div>
								<div class="btn-group offset2 span3">
									<button id="add-target-dataset" class="btn btn-info" type="btn">Select</button>
									<button id="add-target-all-datasets" class="btn btn-primary" type="btn">Select all</button>
									<button id="remove-target-all-datasets" class="btn" type="btn">Remove all</button>
								</div>
							</div>
							<div id="target-catalogue" class="row-fluid"></div>
						</div>
					</div>
				</div>
			</div>
			<div id="catalogue-container" class="row-fluid">
				<div id="div-info" class="span12 well">	
					<div class="row-fluid">
						<div class="span9"><legend class="legend">Browse catalogue : <strong><span id="selected-catalogue"></span></strong></legend></div>
						<div  id="div-search" class="span3">
							<div><strong>Search data items :</strong></div>
							<div class="input-append">
								<input id="search-dataitem" type="text" title="Enter your search term" />
								<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
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
</form>