package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ CaddAnnotatorTest.Config.class, CaddAnnotator.class })
public class CaddAnnotatorTest extends AbstractTestNGSpringContextTests
{

	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	Resources resourcess;

	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

	public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public ArrayList<Entity> input = new ArrayList<>();
	public ArrayList<Entity> input1 = new ArrayList<>();
	public ArrayList<Entity> input2 = new ArrayList<>();
	public ArrayList<Entity> input3 = new ArrayList<>();
	public ArrayList<Entity> input4 = new ArrayList<>();
	public static Entity entity;
	public static Entity entity1;
	public static Entity entity2;
	public static Entity entity3;
	public static Entity entity4;

	public MolgenisSettings settings = mock(MolgenisSettings.class);
	public ArrayList<Entity> entities;

	public void setValues()
	{
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		entities = new ArrayList<>();
		entities.add(entity);
	}

	@BeforeMethod
	public void beforeMethod() throws IOException
	{

		setValues();

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 100);
		entity1.set(VcfRepository.REF, "C");
		entity1.set(VcfRepository.ALT, "T");

		input1.add(entity1);

		entity2.set(VcfRepository.CHROM, "2");
		entity2.set(VcfRepository.POS, new Long(200));
		entity2.set(VcfRepository.REF, "A");
		entity2.set(VcfRepository.ALT, "C");

		input2.add(entity2);

		entity3.set(VcfRepository.CHROM, "3");
		entity3.set(VcfRepository.POS, new Long(300));
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "C");

		input3.add(entity3);

		entity4.set(VcfRepository.CHROM, "1");
		entity4.set(VcfRepository.POS, new Long(100));
		entity4.set(VcfRepository.REF, "T");
		entity4.set(VcfRepository.ALT, "C");

		input4.add(entity4);
	}

	@Test
	public void testThreeOccurencesOneMatch()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(CaddAnnotator.CADD_ABS, -0.03);
		resultMap.put(CaddAnnotator.CADD_SCALED, 2.003);

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input1);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	}

	@Test
	public void testTwoOccurencesNoMatch()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input2);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	}

	@Test
	public void testFourOccurences()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(CaddAnnotator.CADD_ABS, 0.5);
		resultMap.put(CaddAnnotator.CADD_SCALED, 14.5);

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input3);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	}

	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
	}

	@Configuration
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			MolgenisSettings settings = mock(MolgenisSettings.class);
			when(settings.getProperty(CaddAnnotator.CADD_FILE_LOCATION_PROPERTY)).thenReturn(
					ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz").getPath());
			return settings;
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public AnnotationService annotationService()
		{
			return mock(AnnotationService.class);
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}
	}
}
