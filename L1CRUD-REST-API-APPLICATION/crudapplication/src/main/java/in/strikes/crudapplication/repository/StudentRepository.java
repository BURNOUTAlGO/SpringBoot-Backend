package in.strikes.crudapplication.repository;

import in.strikes.crudapplication.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//LAST LAYER - INTERACTION WITH DATABASE
// Repository should be interface and extends jparepository . \
//<student,Long> give <Entity,Primary key type>
//there is no need to write any annotation bacause spring ioc container do not create bean of interfaces
public interface StudentRepository extends JpaRepository<Student,Long> {

}
