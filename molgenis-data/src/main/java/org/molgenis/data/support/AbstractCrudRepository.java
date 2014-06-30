package org.molgenis.data.support;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.validation.EntityValidator;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractCrudRepository extends AbstractRepository implements CrudRepository
{
	private final EntityValidator validator;

	public AbstractCrudRepository(String url, EntityValidator validator)
	{
		super(url);
		this.validator = validator;
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	@Transactional(readOnly = true)
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}

	@Override
	@Transactional
	public void add(Entity entity)
	{
		validator.validate(Arrays.asList(entity), getEntityMetaData(), null);
		addInternal(entity);
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		validator.validate(entities, getEntityMetaData(), null);
		return addInternal(entities);
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		validator.validate(Arrays.asList(entity), getEntityMetaData(), null);
		updateInternal(entity);
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		validator.validate(entities, getEntityMetaData(), null);
		updateInternal(entities);
	}

	@Override
	@Transactional
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		validator.validate(entities, getEntityMetaData(), dbAction);
		updateInternal(entities, dbAction, keyName);
	}

	protected abstract Integer addInternal(Iterable<? extends Entity> entities);

	protected abstract void addInternal(Entity entity);

	protected abstract void updateInternal(Entity entity);

	protected abstract void updateInternal(Iterable<? extends Entity> entities);

	protected abstract void updateInternal(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName);

}
