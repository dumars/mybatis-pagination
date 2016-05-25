# mybatis-pagination
The mybatis pagination plugin and annotation for spring-mvc.
The database support Oracle, MySQL, PostgreSQL.

# HOW TO CONFIG
### Maven
pom.xml
```xml
<repositories>
	<repository>
		<id>mybatis-pagination-mvn-repo</id>
		<url>https://raw.github.com/dumars/mybatis-pagination/mvn-repo/</url>
		<snapshots>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		</snapshots>
	</repository>
</repositories>

<dependencies>
    <dependency>
    	<groupId>com.github.dumars</groupId>
    	<artifactId>mybatis-pagination</artifactId>
    	<version>0.0.2</version>
    </dependency>
</dependencies>
```
### Mybatis
mybatis.xml
  - The "databaseType" property default value is MySQL
  - The "nameRegularExpress" property default value is .*WithPagination.*
```xml
<plugins>
	<plugin interceptor="com.github.dumars.mybatis.pagination.PaginationInterceptor">
		<property name="databaseType" value="PostgreSQL"/>
		<property name="nameRegularExpress" value=".*WithPagination.*"/>
	</plugin>
</plugins>
```
example for UserMapper.xml
```xml
<select id="selectAllWithPagination" resultMap="BaseResultMap">
	select * from "User"
</select>
```
### Spring MVC
spring-servlet.xml
> The interceptor will get pageNo and pageSize from request parameter.

  - pageNo: the current page which you want to display.(if nil value, will be set to zero)
  - pageSize: how many records for one page.

```xml
<mvc:interceptors>
	<mvc:interceptor>
		<mvc:mapping path="/**" />
		<bean class="com.github.dumars.spring.interceptor.PaginationHandlerMethodInterceptor" />
	</mvc:interceptor>
</mvc:interceptors>
```
UserController.java
```java
@Controller
public class UserController {
	
	@Autowired
	private UserMapper userMapper;
	
	@Pagination
	@ResponseBody
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public Paginator list(Model model) {
		return new Paginator(userMapper.selectAllWithPagination());
	}
}
```
  - @Pagination: this annotation is help PaginationHandlerMethodInterceptor to make sure this method should initial some pagination parameters.
  - Paginator: java object, it contains
    + pageNo (int)
    + pageSize (int)
    + totalRows (int)
    + items (Object)
    
javascript example
```javascript
$.get( "/root/users", function( data ) {
  console.log("page number: " + data.pageNo);
  console.log("how many records per page: " + data.pageSize);
  console.log("how many rows of this query: " + data.totalRows);
  console.log("user id: " + data.items[0].id);
  console.log("user name: " + data.items[0].name);
});
```
