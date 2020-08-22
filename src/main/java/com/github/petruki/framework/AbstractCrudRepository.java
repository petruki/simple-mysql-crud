package com.github.petruki.framework;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.framework.annotations.Column;
import com.github.petruki.framework.annotations.Table;
import com.github.petruki.framework.db.DatabaseFactory;
import com.github.petruki.framework.exceptions.DBConnectionException;

public abstract class AbstractCrudRepository<T extends AbstractEntity<?>> {
	
	private static final Logger logger = LogManager.getLogger(AbstractCrudRepository.class);

	protected T entity;

	protected String tableName;

	protected List<String> columns = new ArrayList<>();

	public AbstractCrudRepository(T instance) {
		this.initializeTableStructure(instance);
	}
	
	private void initializeTableStructure(T instance) {
		this.entity = instance;
		Table table = this.entity.getClass().getAnnotation(Table.class);
		this.tableName = table.name();

		for (Field f: this.entity.getClass().getDeclaredFields()) {
			Column column = f.getAnnotation(Column.class);
			if (column != null)
				columns.add(column.name());
		}
	}
	
	public void insert(T entity) throws DBConnectionException {
		try (Connection conn = DatabaseFactory.getConnection()) {
			final PreparedStatement stm = conn.prepareStatement(
					String.format("INSERT INTO %s %s VALUES %s", 
							tableName, 
							createColumns(this.columns, entity), 
							createArgs(this.columns, entity)));

			this.addValues(this.columns, stm, entity);
			stm.executeUpdate();
		} catch (DBConnectionException | SQLException e) {
			throw new DBConnectionException("Insert has errors", e);
		}
	}
	
	public void deleteById(int id) throws DBConnectionException {
		try (Connection conn = DatabaseFactory.getConnection()) {
			final PreparedStatement stm = conn.prepareStatement(
					String.format("DELETE FROM %s WHERE id = ?", tableName));

			stm.setInt(1, id);
			stm.executeUpdate();
		} catch (DBConnectionException | SQLException e) {
			throw new DBConnectionException("Delete has errors", e);
		}
	}
	
	public T queryById(int id) throws DBConnectionException {
		try (Connection conn = DatabaseFactory.getConnection()) {
			final PreparedStatement stm = conn.prepareStatement(
					String.format("SELECT * FROM %s WHERE id = ?", tableName));

			stm.setInt(1, id);
			final ResultSet rst = stm.executeQuery();

			Map<String, Object> valuesMap = new HashMap<>();
			if (rst.next()) {
				for (String column : columns) {
					valuesMap.put(column, rst.getString(column));
				}
				this.setEntity(valuesMap, this.entity);
				return entity;
			}
		} catch (DBConnectionException | SQLException e) {
			throw new DBConnectionException("Query has errors", e);
		}

		return null;
	}
	
	public List<T> query(String column, String value) throws DBConnectionException {
		List<T> resultSet = new ArrayList<>();
		try (Connection conn = DatabaseFactory.getConnection()) {
			PreparedStatement stm = null;
			if (column != null) {
				stm = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s LIKE ?", tableName, column));
				stm.setObject(1, value);
			} else {
				stm = conn.prepareStatement(String.format("SELECT * FROM %s", tableName));
			}

			final ResultSet rst = stm.executeQuery();

			Map<String, Object> valuesMap = new HashMap<>();
			while (rst.next()) {
				for (String col : columns) {
					valuesMap.put(col, rst.getString(col));
				}

				@SuppressWarnings("unchecked")
				T newEntity = (T) entity.newInstance();
				this.setEntity(valuesMap, newEntity);
				resultSet.add(newEntity);
			}
		} catch (DBConnectionException | SQLException e) {
			throw new DBConnectionException("Query has errors", e);
		} catch (Exception e) {
			throw new DBConnectionException("Query has errors", e);
		}

		return resultSet;
	}
	
	public List<T> queryAll() throws DBConnectionException {
		return query(null, null);
	}
	
	public void updateById(int id, T entity) throws DBConnectionException {
		try (Connection conn = DatabaseFactory.getConnection()) {
			final PreparedStatement stm = conn.prepareStatement(
					String.format("UPDATE %s SET %s WHERE id = ?", 
							tableName, createUpdateArgs(this.columns, entity)));

			stm.setInt(1, id);
			stm.executeUpdate();
		} catch (DBConnectionException | SQLException e) {
			throw new DBConnectionException("Update has errors", e);
		}
	}
	
	private String createArgs(List<String> columns, T entity) {
		final StringBuilder builder = new StringBuilder();
		builder.append("(");
		columns.forEach(key -> {
			if (getMapValues(entity).get(key) != null)
				builder.append("?,");
		});
		builder.toString().substring(0, builder.toString().length() - 2);
		return builder.toString().substring(0, builder.toString().length() - 1) + ")";
	}
	
	private String createColumns(List<String> columns, T entity) {
		final StringBuilder builder = new StringBuilder();
		Map<String, Object> valuesMap = getMapValues(entity);
		builder.append("(");
		columns.forEach(key -> {
			if (valuesMap.get(key) != null)
				builder.append(key).append(",");
		});
		builder.toString().substring(0, builder.toString().length() - 2);
		return builder.toString().substring(0, builder.toString().length() - 1) + ")";
	}
	
	private String createUpdateArgs(List<String> columns, T entity) {
		final StringBuilder builder = new StringBuilder();
		final Map<String, Object> values = getMapValues(entity);
		columns.forEach(key -> builder.append(key)
				.append(" = '")
				.append(values.get(key))
				.append("',"));

		return builder.toString().substring(0, builder.toString().length() - 1);
	}
	
	private void addValues(List<String> columns, PreparedStatement stm, T entity) 
			throws DBConnectionException {
		int columnPos = 1;
		final Map<String, Object> values = getMapValues(entity);
		for (String column : columns) {
			try {
				if (values.get(column) != null) {
					if (values.get(column).getClass().equals(String.class)) {
						stm.setString(columnPos++, values.get(column).toString());
					} else if (values.get(column).getClass().equals(Integer.class)) {
						stm.setInt(columnPos++, Integer.parseInt(values.get(column).toString()));
					} else if (values.get(column).getClass().equals(Date.class)) {
						stm.setDate(columnPos++, (Date) values.get(column)); 
					} else if (values.get(column).getClass().equals(Float.class)) {
						stm.setFloat(columnPos++, Float.parseFloat(values.get(column).toString()));
					} else if (values.get(column).getClass().equals(Double.class)) {
						stm.setDouble(columnPos++, Double.parseDouble(values.get(column).toString()));
					} else if (values.get(column).getClass().equals(Boolean.class)) {
						stm.setBoolean(columnPos++, Boolean.parseBoolean(values.get(column).toString()));
					}
				}
			} catch (SQLException e) {
				throw new DBConnectionException("addValue has errors", e);
			}
		}
	}
	
	private Map<String, Object> getMapValues(T entity) {
		final Map<String, Object> values = new HashMap<>();

		for (Field f: entity.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Column column = f.getAnnotation(Column.class);
			if (column != null) {
				try {
					final Object value = f.get(entity);
					values.put(column.name(), value);
				} catch (Exception e) {
					logger.error(String.format("It was not possible get the value from: %s - %s", 
							column.name(), e.getMessage()));
				}
			}
		}
		return values;
	}
	
	private void setEntity(Map<String, Object> values, T entity) {
		for (Field f: entity.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Column column = f.getAnnotation(Column.class);
			if (column != null && values.get(column.name()) != null) {
				try {
					if (f.getType().isPrimitive()) {
						if (f.getType().toString().equals("int")) {
							f.setInt(entity, Integer.parseInt(values.get(column.name()).toString()));
						} else if (f.getType().toString().equals("boolean")) {
							f.setBoolean(entity, Boolean.parseBoolean(values.get(column.name()).toString()));
						} else if (f.getType().toString().equals("byte")) {
							f.setByte(entity, Byte.parseByte(values.get(column.name()).toString()));
						} else if (f.getType().toString().equals("char")) {
							f.setChar(entity, (char) values.get(column.name()).toString().charAt(0));
						} else if (f.getType().toString().equals("double")) {
							f.setDouble(entity, Double.parseDouble(values.get(column.name()).toString()));
						} else if (f.getType().toString().equals("float")) {
							f.setFloat(entity, Float.parseFloat(values.get(column.name()).toString()));
						}
					} else if (f.getType().isAssignableFrom(Date.class)) {
						f.set(entity, Date.valueOf(values.get(column.name()).toString()));
					} else if (f.getType().isAssignableFrom(Float.class)) {
						f.set(entity, Float.valueOf(values.get(column.name()).toString()));
					} else {
						f.set(entity, values.get(column.name()));
					}
				} catch (Exception e) {
					logger.error(
							String.format("It was not possible set the value from: %s - %s", 
									column.name(), e.getMessage()));
				}
			}
		}
	}

}
