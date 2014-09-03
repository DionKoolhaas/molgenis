package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/** Test for MolgenisFieldTypes.MREF */
public class MysqlRepositoryMrefTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData refEntity = new DefaultEntityMetaData("StringTarget2");
		refEntity.setLabelAttribute("label");
		refEntity.setIdAttribute("identifier");
		refEntity.addAttribute("identifier").setNillable(false);

		DefaultEntityMetaData refEntity2 = new DefaultEntityMetaData("IntTarget2");
		refEntity2.setIdAttribute("identifier");
		refEntity2.addAttribute("identifier").setDataType(MolgenisFieldTypes.INT).setNillable(false);

		DefaultEntityMetaData varcharMD = new DefaultEntityMetaData("MrefTest").setLabel("ref Test");
		varcharMD.setIdAttribute("identifier");
		varcharMD.addAttribute("identifier").setNillable(false);
		varcharMD.addAttribute("stringRef").setDataType(MolgenisFieldTypes.MREF).setRefEntity(refEntity)
				.setNillable(false);
		varcharMD.addAttribute("intRef").setDataType(MolgenisFieldTypes.MREF).setRefEntity(refEntity2);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `MrefTest`(`identifier` VARCHAR(255) NOT NULL, PRIMARY KEY (`identifier`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		return null;
	}

	@Override
	@Test
	public void test() throws Exception
	{
		coll.dropEntityMetaData(getMetaData().getName());
		coll.dropEntityMetaData(getMetaData().getAttribute("stringRef").getRefEntity().getName());
		coll.dropEntityMetaData(getMetaData().getAttribute("intRef").getRefEntity().getName());

		// create
		MysqlRepository stringRepo = coll.add(getMetaData().getAttribute("stringRef").getRefEntity());
		MysqlRepository intRepo = coll.add(getMetaData().getAttribute("intRef").getRefEntity());
		MysqlRepository mrefRepo = coll.add(getMetaData());

		Assert.assertEquals(stringRepo.count(), 0);
		Assert.assertEquals(intRepo.count(), 0);
		Assert.assertEquals(mrefRepo.count(), 0);

		Assert.assertEquals(mrefRepo.getCreateSql(), createSql());

		// add records
		Entity entity = new MapEntity();
		entity.set("identifier", "ref1");
		stringRepo.add(entity);

		entity.set("identifier", "ref2");
		stringRepo.add(entity);

		entity.set("identifier", "ref3");
		stringRepo.add(entity);

		entity.set("identifier", 1);
		intRepo.add(entity);

		entity.set("identifier", 2);
		intRepo.add(entity);

		entity.set("identifier", "one");
		entity.set("stringRef", Arrays.asList(new String[]
		{ "ref1", "ref2" }));
		entity.set("intRef", Arrays.asList(new Integer[]
		{ 1, 2 }));
		logger.debug("mref: " + entity);
		mrefRepo.add(entity);

		entity.set("identifier", "two");
		entity.set("stringRef", "ref3");
		entity.set("intRef", null);
		logger.debug("mref: " + entity);
		mrefRepo.add(entity);

		Assert.assertEquals(mrefRepo.count(), 2);

		Assert.assertEquals(
				mrefRepo.getSelectSql(new QueryImpl(), Lists.newArrayList()),
				"SELECT this.`identifier`, GROUP_CONCAT(DISTINCT(`stringRef`.`stringRef`)) AS `stringRef`, GROUP_CONCAT(DISTINCT(`intRef`.`intRef`)) AS `intRef` FROM `MrefTest` AS this LEFT JOIN `MrefTest_stringRef` AS `stringRef` ON (this.`identifier` = `stringRef`.`identifier`) LEFT JOIN `MrefTest_intRef` AS `intRef` ON (this.`identifier` = `intRef`.`identifier`) GROUP BY this.`identifier`");

		assertEquals(mrefRepo.query().eq("identifier", "one").count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("identifier", "one")))
		{
			logger.info("found: " + e);
			Assert.assertEquals(e.getList("stringRef"), Arrays.asList(new String[]
			{ "ref1", "ref2" }));
			Assert.assertEquals(e.getList("intRef"), Arrays.asList(new Integer[]
			{ 1, 2 }));

			List<Entity> result = new ArrayList<Entity>();
			for (Entity e2 : e.getEntities("stringRef"))
			{
				result.add(e2);
			}
			Assert.assertEquals(result.get(0).getString("identifier"), "ref1");
			Assert.assertEquals(result.get(1).getString("identifier"), "ref2");
		}

		assertEquals(mrefRepo.query().eq("stringRef", "ref3").count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("stringRef", "ref3")))
		{
			logger.debug("found: " + e);
			Assert.assertEquals(e.get("stringRef"), Arrays.asList(new String[]
			{ "ref3" }));
		}

		assertEquals(mrefRepo.query().eq("stringRef", "ref1").count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("stringRef", "ref1")))
		{
			logger.debug("found: " + e);
			Object obj = e.get("stringRef");
			assertTrue(obj instanceof List<?>);
			Assert.assertEquals(Sets.newHashSet((List<?>) obj), Sets.newHashSet(new String[]
			{ "ref1", "ref2" }));
		}

		assertEquals(mrefRepo.query().gt("intRef", 1).count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().gt("intRef", 1)))
		{
			logger.debug("found: " + e);
			Assert.assertEquals(e.get("intRef"), Arrays.asList(new Integer[]
			{ 1, 2 }));
		}

		assertEquals(mrefRepo.query().eq("stringRef", "ref1").and().eq("stringRef", "ref2").count(), Long.valueOf(1));
		assertEquals(mrefRepo.query().eq("intRef", 1).and().eq("intRef", 2).count(), Long.valueOf(1));
		assertEquals(mrefRepo.query().in("stringRef", Arrays.asList("ref1", "ref2")).count(), Long.valueOf(1));
		assertEquals(mrefRepo.query().in("intRef", Arrays.asList(1, 2)).count(), Long.valueOf(1));

		// update

		Entity e = mrefRepo.findOne("one");
		e.set("stringRef", "ref2,ref3");
		mrefRepo.update(e);

		e = mrefRepo.findOne("one");
		Assert.assertEquals(e.getList("stringRef").size(), 2);
		Assert.assertTrue(e.getList("stringRef").contains("ref2"));
		Assert.assertTrue(e.getList("stringRef").contains("ref3"));

		// verify not null error

		// verify default
	}
}
