package org.molgenis.mutationdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MysqlViewService
{
	private static final Logger LOG = LoggerFactory.getLogger(MysqlViewService.class);

	@Autowired
	private DataService dataService;

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public MysqlViewService(DataSource dataSource)
	{
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Return the values map to headers
	 * 
	 * @param headers
	 * @param iterable
	 * @return
	 */
	public Map<String, List<Value>> valuesPerHeader(List<String> headers, Iterable<Entity> iterable)
	{
		Iterator<Entity> iterator = iterable.iterator();
		Map<String, List<Value>> valuesMap = new HashMap<String, List<Value>>();
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			for (String header : headers)
			{
				if (!valuesMap.containsKey(header))
				{
					valuesMap.put(header, new ArrayList<Value>());
				}

				if (entity.get(header) != null)
				{
					valuesMap.get(header).add(new Value(entity.get(header).toString()));
				}
				else
				{
					valuesMap.get(header).add(new Value(""));
				}
			}
		}

		return valuesMap;
	}

	/**
	 * Merges the values if possible and produces a row. per header all values must be the same to be merged.
	 * 
	 * @param idHeader
	 *            can only contain one value.
	 * @param headers
	 *            the names of the headers to be merged and be added to row
	 * @param valuesByHeader
	 * @return
	 */
	public Row createRowByMergingValuesIfEquales(List<String> headers, Map<String, List<Value>> valuesByHeader)
	{
		Row row = new Row();
		for (String header : headers)
		{
			List<Value> values = valuesByHeader.get(header);
			if (null != values)
			{
				Cell cell = new Cell();
				if (values.isEmpty())
				{
					row.add(cell);
					continue;
				}

				boolean equals = areAllValuesEquals(values);
				if (equals)
				{
					cell.add(values.get(0));
				}
				else
				{
					cell.addAll(values);
				}
				row.add(cell);
			}
		}

		return row;
	}

	/**
	 * Add values row.
	 * 
	 * @param idHeader
	 *            can only contain one value.
	 * @param headers
	 *            the names of the headers
	 * @param valuesByHeader
	 * @return
	 */
	public Row createRow(List<String> headers, Map<String, List<Value>> valuesByHeader)
	{
		Row row = new Row();
		for (String header : headers)
		{
			List<Value> values = valuesByHeader.get(header);
			if (null != values)
			{
				Cell cell = new Cell();
				cell.addAll(values);
				row.add(cell);
			}
		}

		return row;
	}

	/**
	 * Create row from entity
	 * 
	 * @param headers
	 * @param entity
	 * @return
	 */
	public Row createRow(List<String> headers, Entity entity)
	{
		Row row = new Row();

		for (String header : headers)
		{
			final Value value;
			if (null != entity.get(header))
			{
				value = new Value(entity.get(header).toString());
			}
			else
			{
				value = new Value("");
			}

			final Cell cell = new Cell();
			cell.add(value);
			row.add(cell);
		}

		return row;
	}

	public boolean areAllValuesEquals(List<Value> values)
	{
		if (values.isEmpty()) return false;

		Value lastValue = null;
		for (Value value : values)
		{
			if (lastValue == null)
			{
				lastValue = value;
			}

			if (!lastValue.equals(value))
			{
				return false;
			}
		}

		return true;
	}

	public void truncate(String tableName)
	{
		jdbcTemplate.execute(getTruncateSql(tableName));
	}

	protected String getTruncateSql(String tableName)
	{
		return "TRUNCATE TABLE " + tableName + ";";
	}

	public void populateWithQuery(String insertQuery)
	{
		try
		{
			jdbcTemplate.execute(insertQuery);
		}
		catch (Exception e)
		{
			LOG.error("Exception executing query: [" + insertQuery + "]", e);
			throw new MolgenisDataException(e);
		}
	}
}
