package org.molgenis.omx.importer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.importer.EntityImportService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.dataset.DataSetMatrixRepository;
import org.molgenis.omx.observ.CategoryRepository;
import org.molgenis.omx.observ.CharacteristicRepository;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.DataSetRepository;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservableFeatureRepository;
import org.molgenis.omx.observ.ObservationSetRepository;
import org.molgenis.omx.observ.ObservationTargetRepository;
import org.molgenis.omx.observ.ObservedValueRepository;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.ProtocolRepository;
import org.molgenis.omx.observ.target.IndividualRepository;
import org.molgenis.omx.observ.target.PanelRepository;
import org.molgenis.omx.observ.value.BoolValueRepository;
import org.molgenis.omx.observ.value.CategoricalValueRepository;
import org.molgenis.omx.observ.value.DateTimeValueRepository;
import org.molgenis.omx.observ.value.DateValueRepository;
import org.molgenis.omx.observ.value.DecimalValueRepository;
import org.molgenis.omx.observ.value.EmailValueRepository;
import org.molgenis.omx.observ.value.HtmlValueRepository;
import org.molgenis.omx.observ.value.HyperlinkValueRepository;
import org.molgenis.omx.observ.value.IntValueRepository;
import org.molgenis.omx.observ.value.LongValueRepository;
import org.molgenis.omx.observ.value.MrefValueRepository;
import org.molgenis.omx.observ.value.StringValueRepository;
import org.molgenis.omx.observ.value.TextValueRepository;
import org.molgenis.omx.observ.value.ValueRepository;
import org.molgenis.omx.observ.value.XrefValueRepository;
import org.molgenis.search.SearchService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OmxImporterServiceTest
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	protected DataService dataService;
	private EntityManager entityManager;
	private OmxImporterService importer;
	private EmbeddedElasticSearchServiceFactory factory;
	private SearchService searchService;
	private FileRepositoryCollectionFactory fileRepositorySourceFactory;

	@BeforeMethod
	public void beforeMethod()
	{
		entityManager = Persistence.createEntityManagerFactory("molgenis-import-test").createEntityManager();
		dataService = new DataServiceImpl();
		fileRepositorySourceFactory = new FileRepositoryCollectionFactory();

		fileRepositorySourceFactory.addFileRepositoryCollectionClass(ExcelRepositoryCollection.class,
				ExcelRepositoryCollection.EXTENSIONS);
		EntityValidator validator = new DefaultEntityValidator(dataService, new EntityAttributesValidator());
		QueryResolver queryResolver = new QueryResolver(dataService);

		dataService.addRepository(new CharacteristicRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new ObservableFeatureRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new ProtocolRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new DataSetRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new ObservationSetRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new ObservedValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new CategoryRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new ObservationTargetRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new IndividualRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new PanelRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new ValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new StringValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new XrefValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new MrefValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new EmailValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new DecimalValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new IntValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new CategoricalValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new DateValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new DateTimeValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new BoolValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new HtmlValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new HyperlinkValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new LongValueRepository(entityManager, validator, queryResolver));
		dataService.addRepository(new TextValueRepository(entityManager, validator, queryResolver));

		factory = new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", "target/data"));
		searchService = factory.create();

		EntityImportService eis = new EntityImportService();
		eis.setDataService(dataService);
		//importer = new OmxImporterServiceImpl(dataService, searchService, new EntitiesImporterImpl(
		//		fileRepositorySourceFactory, eis), validator, new QueryResolver(dataService));

		entityManager.getTransaction().begin();

		// set super user credentials
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}

	@Test
	public void testImportSampleOmx() throws IOException, ValueConverterException
	{
		RepositoryCollection source = fileRepositorySourceFactory
				.createFileRepositoryCollection(loadTestFile("example-omx.xls"));
		importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);

		assertEquals(dataService.count(DataSet.ENTITY_NAME, new QueryImpl()), 1);
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl(), DataSet.class);
		searchService.indexRepository(new DataSetMatrixRepository(dataService, dataSet.getIdentifier()));
		searchService.refresh();

		assertEquals(dataService.count(ObservableFeature.ENTITY_NAME, new QueryImpl()), 18);
		assertEquals(dataService.count(Protocol.ENTITY_NAME, new QueryImpl()), 6);
		assertEquals(dataService.count("celiacsprue", new QueryImpl()), 4);

		Entity patient44 = dataService.findOne("celiacsprue", new QueryImpl().eq("Celiac_Individual", "id_103"));
		assertNotNull(patient44);
	}

	@Test
	public void testImportMissingXref() throws IOException, ValueConverterException
	{
		try
		{
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("missing-xref.xlsx"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getRownr(), 2);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Klaas");
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "father");
		}
	}

	@Test
	public void testImportMissingMref() throws IOException, ValueConverterException
	{
		try
		{
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("missing-mref.xlsx"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getRownr(), 3);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Jaap,Klaas,Marie");
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "children");
		}
	}

	@Test
	public void testImportWrongEmailValue() throws IOException, ValueConverterException
	{
		try
		{
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("wrong-email-value.xlsx"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 2);
			assertEquals(e.getViolations().iterator().next().getRownr(), 2);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Klaas");
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "email");
		}
	}

	@Test
	public void testFeatureInMultipleProtocols() throws IOException, ValueConverterException
	{
		try
		{
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("feature-in-multiple-protocols.xls"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Celiac_Individual");
		}
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		factory.close();
		entityManager.getTransaction().rollback();
		entityManager.close();
	}

	private File loadTestFile(String name) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/" + name);
		File f = File.createTempFile(name, "." + StringUtils.getFilenameExtension(name));
		FileCopyUtils.copy(in, new FileOutputStream(f));

		return f;
	}

}
