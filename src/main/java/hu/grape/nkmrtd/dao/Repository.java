package hu.grape.nkmrtd.dao;

import java.util.List;

public interface Repository<T> {
    T findById(final Long id);

    List<T> findAll();

    List<T> findEntitiesWithLimitOrderByCreated(final int limit);

    Long save(final T entity);

    void saveAll(final List<T> entities);

    void update(final T entity);

    void delete(final Long id);

    void deleteAll();
}
