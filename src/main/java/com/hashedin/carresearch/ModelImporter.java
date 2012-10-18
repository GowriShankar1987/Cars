package com.hashedin.carresearch;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ModelImporter 
{

		private final JdbcTemplate jdbcTemplate;
		
		private static final String INSERT_MODEL_QUERY = "insert into models (make, modelname, year, maxPrice, minPrice) "
		+ "values (?, ?, ?, ?, ?)";
		
		public static void main(String args[]) throws IOException 
		{
				XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
				DataSource dataSource = factory.getBean(DataSource.class);
				
				ModelImporter importer = new ModelImporter(dataSource);
				
				InputStream rawXMlStream = CarResearch.getRawInputStream();
				ModelParser parser = new DomParser();
				List<Model> models = parser.parse(rawXMlStream);
				//importer.insert(models);
				importer.getMaxModel();
				importer.getMinModel();
		}
		
		public  void insert(List<Model> cars){
			for(Model m: cars){
				jdbcTemplate.update(INSERT_MODEL_QUERY, new Object[] {m.getMake(),m.getModelName(), m.getYear(),m.getMaxPrice(),m.getMinPrice()});
			}
		}
		
		public ModelImporter(DataSource dataSource) {
			this.jdbcTemplate = new JdbcTemplate(dataSource);
		}
		public void getMaxModel()
		{
			Model maxModel = jdbcTemplate.queryForObject("select * from models where minPrice = (select max(minPrice) from models) ", new ModelMapper());
					System.out.println(maxModel);
		}
		
		public void getMinModel()
		{
			List<Model> minModels = jdbcTemplate.query("select * from models where minPrice = (select min(minPrice) from models) ",new ModelMapper());
			for(Model m : minModels)
			{
					System.out.println(m);
			}
		}
		
		private void printModels() 
		{
				List<Model> allModels = jdbcTemplate.query("select make, modelname, year, maxPrice, " +
				"minPrice from models", new ModelMapper());
				
				for(Model m : allModels) {
				System.out.println(m);
				}
		}
}

final class ModelMapper implements RowMapper<Model> {

	public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
		Model m = new Model();
		m.setMake(rs.getString(1));
		m.setModelName(rs.getString(2));
		m.setYear(rs.getString(3));
		m.setMaxPrice(rs.getDouble(4));
		m.setMinPrice(rs.getDouble(5));
		return m;
		}
}