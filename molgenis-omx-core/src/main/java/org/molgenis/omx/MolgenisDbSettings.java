package org.molgenis.omx;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "We want to return Boolean.TRUE, Boolean.FALSE or null")
public class MolgenisDbSettings implements MolgenisSettings
{
	private static final Logger logger = Logger.getLogger(MolgenisDbSettings.class);

	private final DataService dataService;

	@Autowired
	public MolgenisDbSettings(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	@RunAsSystem
	public String getProperty(String key)
	{
		return getProperty(key, null);
	}

	@Override
	@RunAsSystem
	public String getProperty(String key, String defaultValue)
	{
		Query propertyRule = new QueryImpl().eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName() + '_'
				+ key);

		RuntimeProperty property;
		try
		{
			property = dataService.findOne(RuntimeProperty.ENTITY_NAME, propertyRule, RuntimeProperty.class);
		}
		catch (MolgenisDataException e)
		{
			logger.warn(e);
			return defaultValue;
		}

		if (property == null)
		{
			logger.debug(RuntimeProperty.class.getSimpleName() + " '" + key + "' is null");
			return defaultValue;
		}

		return property.getValue();
	}

	@Override
	public void setProperty(String key, String value)
	{
		String identifier = RuntimeProperty.class.getSimpleName() + '_' + key;

		RuntimeProperty property = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				new QueryImpl().eq(RuntimeProperty.IDENTIFIER, identifier), RuntimeProperty.class);

		if (property == null)
		{
			property = new RuntimeProperty();
			property.setIdentifier(identifier);
			property.setName(key);
			property.setValue(value);
			dataService.add(RuntimeProperty.ENTITY_NAME, property);
		}
		else
		{
			property.setValue(value);
			dataService.update(RuntimeProperty.ENTITY_NAME, property);
		}
	}

	@Override
	public Boolean getBooleanProperty(String key)
	{
		String value = getProperty(key);
		if (value == null)
		{
			return null;
		}

		return Boolean.valueOf(value);
	}

	@Override
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		Boolean value = getBooleanProperty(key);
		if (value == null)
		{
			return defaultValue;
		}

		return value;
	}

	@Override
	@RunAsSystem
	public boolean updateProperty(String key, String content)
	{
		if (null == content)
		{
			throw new IllegalArgumentException("content is null");
		}

		Query query = new QueryImpl().eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName() + '_' + key);
		try
		{
			RuntimeProperty property = dataService.findOne(RuntimeProperty.ENTITY_NAME, query, RuntimeProperty.class);
			if (property != null)
			{
				property.setValue(content);
				dataService.update(RuntimeProperty.ENTITY_NAME, property);
				return true;
			}
		}
		catch (MolgenisDataException e)
		{
			logger.warn(e);
		}

		return false;
	}

	@Override
	public boolean propertyExists(String key)
	{
		long count = dataService.count(RuntimeProperty.ENTITY_NAME,
				new QueryImpl().eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName() + '_' + key));
		if (count > 0)
		{
			return true;
		}

		return false;
	}

	@Override
	@RunAsSystem
	public Map<String, String> getProperties(String keyStartsWith)
	{
		String prefix = RuntimeProperty.class.getSimpleName() + '_' + keyStartsWith;
		QueryImpl query = new QueryImpl();
		query.addRule(new QueryRule("identifier", Operator.LIKE, prefix));
		Iterable<RuntimeProperty> properties = dataService.findAll(RuntimeProperty.ENTITY_NAME, query,
				RuntimeProperty.class);
		Map<String, String> result = new TreeMap<String, String>();
		for (RuntimeProperty property : properties)
		{
			// LIKE does not guarantee that the prefix occurs at the start of the identifier
			if (property.getIdentifier().startsWith(prefix))
			{
				result.put(property.getIdentifier().substring(prefix.length() + 1), property.getValue());
			}
		}
		return result;
	}
}
