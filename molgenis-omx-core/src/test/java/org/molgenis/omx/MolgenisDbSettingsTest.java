package org.molgenis.omx;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.core.RuntimeProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration
public class MolgenisDbSettingsTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public MolgenisDbSettings molgenisDbSettings()
		{

			return new MolgenisDbSettings(dataService());
		}

		private static RuntimeProperty createRuntimeProperty(String name, String value){
			RuntimeProperty result = new RuntimeProperty();
			result.setIdentifier("RuntimeProperty_"+name);
			result.setName(name);
			result.setValue(value);
			return result;
		}
		
		@Bean
		public DataService dataService()
		{
			DataService dataservice = mock(DataService.class);
			Query q = new QueryImpl().eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName()
					+ "_property0");
			RuntimeProperty property0 = new RuntimeProperty();
			property0.setValue("value0");
			property0.setName("property0");
			property0.setIdentifier(RuntimeProperty.class.getSimpleName() + "_property0");
			when(dataservice.findOne(RuntimeProperty.ENTITY_NAME, q, RuntimeProperty.class)).thenReturn(property0);

			Query q1 = new QueryImpl().eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName()
					+ "_property1");
			RuntimeProperty property1 = new RuntimeProperty();
			property1.setValue("true");
			when(dataservice.findOne(RuntimeProperty.ENTITY_NAME, q1, RuntimeProperty.class)).thenReturn(property1);

			Query q2 = new QueryImpl().eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName()
					+ "_property2");
			RuntimeProperty property2 = new RuntimeProperty();
			property2.setValue("false");
			when(dataservice.findOne(RuntimeProperty.ENTITY_NAME, q2, RuntimeProperty.class)).thenReturn(property2);
			
			when(
					dataservice.findAll(
							eq(RuntimeProperty.ENTITY_NAME),
							argThat(allOf(
									isA(Query.class),
									hasProperty("rules", contains(allOf(
											hasProperty("field", equalTo("identifier")),
											hasProperty("operator", equalTo(Operator.LIKE)),
											hasProperty("value", equalTo("RuntimeProperty_plugin.dataexplorer"))))))),
							eq(RuntimeProperty.class))).thenReturn(
					Arrays.asList(createRuntimeProperty("plugin.dataexplorer.editable", "true"),
							createRuntimeProperty("plugin.dataexplorer.genomebrowser.data.desc", "INFO"),
							createRuntimeProperty("prefix.RuntimeProperty_plugin.dataexplorer", "farfetched case that must be filtered out")));

			return dataservice;
		}
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisDbSettings molgenisDbSettings;

	@Test
	public void getPropertyString()
	{
		assertEquals(molgenisDbSettings.getProperty("property0"), "value0");
	}

	@Test
	public void getPropertyString_unknownProperty()
	{
		assertNull(molgenisDbSettings.getProperty("unknown-property"));
	}

	@Test
	public void getPropertyStringString()
	{
		assertEquals(molgenisDbSettings.getProperty("property0", "default-value"), "value0");
	}

	@Test
	public void getPropertyStringString_unknownProperty()
	{
		assertEquals(molgenisDbSettings.getProperty("unknown-property", "default-value"), "default-value");
	}

	@Test
	public void getPropertyBooleanTrue()
	{
		assertTrue(molgenisDbSettings.getBooleanProperty("property1"));
	}

	@Test
	public void getPropertyBooleanFalse()
	{
		assertFalse(molgenisDbSettings.getBooleanProperty("property2"));
	}

	@Test
	public void getBooleanProperty_unknownProperty()
	{
		assertNull(molgenisDbSettings.getBooleanProperty("unknown-property"));
	}

	@Test
	public void getBooleanProperty_unknownProperty_with_default()
	{
		assertTrue(molgenisDbSettings.getBooleanProperty("unknown-property", true));
	}

	@Test
	public void setProperty()
	{
		molgenisDbSettings.setProperty("property0", "value0-updated");

		RuntimeProperty property0 = new RuntimeProperty();
		property0.setIdentifier(RuntimeProperty.class.getSimpleName() + "_property0");
		property0.setName("property0");
		property0.setValue("value0-updated");
		verify(dataService).update(RuntimeProperty.ENTITY_NAME, property0);
	}
	
	@Test void getProperties()
	{
		Map<String, String> expected = new TreeMap<String, String>();
		expected.put("editable", "true");
		expected.put("genomebrowser.data.desc","INFO");
		assertEquals(expected, molgenisDbSettings.getProperties("plugin.dataexplorer"));
	}
}
