package org.molgenis.ui.migrate.v1_5;

import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.ui.menu.MenuItemType.MENU;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.base.Joiner.MapJoiner;
import org.molgenis.data.Repository;
import org.molgenis.data.version.MolgenisUpgrade;
import org.molgenis.system.core.RuntimeProperty;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeTraverser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Updates dataexplorer menu items from <code>dataset</code> to <code>entity</code>.
 * 
 * @see <a href=https://github.com/molgenis/molgenis/issues/2769>#2769</a>
 * 
 *      JSON form of the menu items to upgrade is <code>{
            "type": "plugin",
            "id": "dataexplorer",
            "label": "TypeTest",
            "params": "entity\u003dorg_molgenis_test_TypeTest"
        }</code>
 */
public class Step5AlterDataexplorerMenuURLs extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step5AlterDataexplorerMenuURLs.class);

	final MapSplitter splitter = Splitter.on("&").withKeyValueSeparator("=");
	final MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=");
	final Gson gson = new GsonBuilder().create();
	final Repository rtpRepo;

	public Step5AlterDataexplorerMenuURLs(Repository rtpRepo)
	{
		super(4, 5);
		this.rtpRepo = rtpRepo;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Migrating the menu...");
		RuntimeProperty rtp = new RuntimeProperty();
		rtp.set(rtpRepo.findOne(EQ("Name", "molgenis.menu")));
		Menu menu = gson.fromJson(rtp.getValue(), Menu.class);
		List<MenuItem> plugins = getPluginsFromMenu(menu);
		plugins.stream().filter(p -> StringUtils.equals("dataexplorer", p.getId()))
				.forEach(this::migrateDataExplorerPlugin);
		rtp.setValue(gson.toJson(menu));
		rtpRepo.update(rtp);
		LOG.info("Migrating the menu DONE.");
	}

	void migrateDataExplorerPlugin(MenuItem plugin)
	{
		LOG.info("Migrating dataexplorer plugin {}...", plugin.getLabel());
		String params = plugin.getParams();
		LOG.info("params: {}", params);
		if (params != null)
		{
			plugin.setParams(transformParams(params));
		}
	}

	String transformParams(String params)
	{
		Map<String, String> queryParameters = splitter.split(params);
		LOG.info("queryParameters: {}", queryParameters);
		Map<String, String> transformedQueryParameters = queryParameters.entrySet().stream()
				.collect(Collectors.toMap(entry -> transformKey(entry.getKey()), Map.Entry::getValue));
		LOG.info("transformedQueryParameters: {}", transformedQueryParameters);
		return joiner.join(transformedQueryParameters);
	}

	private String transformKey(String key)
	{
		return StringUtils.equals(key, "dataset") ? "entity" : key;
	}

	/**
	 * Retrieves all menu items of type {@link MenuItemType#PLUGIN}.
	 * 
	 * @param menu
	 * 
	 * @return {@link ImmutableList} of {@link MenuItem}s
	 */
	private List<MenuItem> getPluginsFromMenu(Menu menu)
	{
		return new TreeTraverser<MenuItem>()
		{
			@Override
			public Iterable<MenuItem> children(MenuItem item)
			{
				if (item.getType() == MENU)
				{
					return item.getItems();
				}
				else
				{
					return Collections.<MenuItem> emptyList();
				}
			}
		}.preOrderTraversal(menu).toList().stream().filter(item -> item.getType() == MenuItemType.PLUGIN)
				.collect(Collectors.toList());
	}
}
