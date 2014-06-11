package org.molgenis.col7a1;

import static org.molgenis.col7a1.MutationsViewController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class MutationsViewController extends MolgenisPluginController
{
	public static final String ID = "col7a1_mutations";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String ENTITYNAME_MUTATIONSVIEW = "import_mutationsview";
	private static final String ENTITYNAME_MUTATIONS = "import_mutations";
	private static final String MUTATIONS__MUTATION_ID = "identifier_mutation";
	private static final String MUTATIONSVIEW__MUTATION_ID = "Subject Mutation ID";
	private static final String TITLE = "Mutations view";
	private final DataService dataService;
	private final MysqlViewService mysqlViewService;
	private BufferedReader bufferedReader;
	private static List<String> HEADERS_NAMES = Arrays.asList("Mutation ID", "cDNA change", "Protein change",
			"Exon/Intron", "Consequence",
			"Inheritance", "Patient ID", "Phenotype");
	private static final String PATH_TO_INSERT_QUERY = File.separator + "mysql" + File.separator
			+ "mutationview_col7a1_prototype.sql";
	private static final String PATH_TO_NA_QUERY = File.separator + "mysql" + File.separator
			+ "mutationview_col7a1_prototype_update_na.sql";

	@Autowired
	public MutationsViewController(DataService dataService, MysqlViewService mysqlViewService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;

		if (mysqlViewService == null) throw new IllegalArgumentException("mysqlViewService is null");
		this.mysqlViewService = mysqlViewService;
	}


	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("title", TITLE);
		return "view-col7a1";
	}

	@RequestMapping(value = "/create", method = GET, produces = APPLICATION_JSON_VALUE)
	public String create(Model model)
	{
		List<Row> rows = null;
		if (dataService.hasRepository(ENTITYNAME_MUTATIONSVIEW) && dataService.hasRepository(ENTITYNAME_MUTATIONS))
		{
			MysqlRepository mutationsViewRepo = (MysqlRepository) dataService
					.getRepositoryByEntityName(ENTITYNAME_MUTATIONSVIEW);
			MysqlRepository mutationsRepo = (MysqlRepository) dataService
					.getRepositoryByEntityName(ENTITYNAME_MUTATIONS);
			rows = createRows(mutationsViewRepo, mutationsRepo);
		}
		model.addAttribute("rows", rows);
		model.addAttribute("headers", HEADERS_NAMES);
		return "view-col7a1-table";
	}

	@RequestMapping(value = "/generate", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public boolean refresh(Model model)
	{
		if (dataService.hasRepository(ENTITYNAME_MUTATIONSVIEW))
		{
			MysqlRepository mutationsViewRepo = (MysqlRepository) dataService
					.getRepositoryByEntityName(ENTITYNAME_MUTATIONSVIEW);
			mutationsViewRepo.truncate();
			mutationsViewRepo.populateWithQuery(mutationsViewRepo.getMySqlQueryFromFile(PATH_TO_INSERT_QUERY));
			mutationsViewRepo.populateWithQuery(mutationsViewRepo.getMySqlQueryFromFile(PATH_TO_NA_QUERY));
			return true;
		}
		else
		{
			return false;
		}
	}

	private List<Row> createRows(MysqlRepository mutationsViewRepo, MysqlRepository mutationsRepo)
	{
		Iterator<Entity> iterator = mutationsRepo.iterator();
		List<Row> rows = new ArrayList<Row>();
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			String mutationId = entity.getString(MUTATIONS__MUTATION_ID);
			if (null != mutationId)
			{
				Iterable<Entity> iterable = mutationsViewRepo.findAll(new QueryImpl().eq(MUTATIONSVIEW__MUTATION_ID,
						mutationId));
				Map<String, List<Value>> valuesPerHeader = this.mysqlViewService.valuesPerHeader(HEADERS_NAMES,
						iterable);

				Row row = this.mysqlViewService.createRow(HEADERS_NAMES, valuesPerHeader);
				rows.add(row);
			}
		}

		return rows;
	}
}
