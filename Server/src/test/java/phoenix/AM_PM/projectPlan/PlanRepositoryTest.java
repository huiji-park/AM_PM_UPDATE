package phoenix.AM_PM.projectPlan;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import phoenix.AM_PM.domain.projectplan.entity.ProjectPlan;
import phoenix.AM_PM.domain.projectplan.repository.ProjectPlanRepository;

import java.util.List;

@DataJpaTest
public class PlanRepositoryTest {

    @Autowired
    private ProjectPlanRepository projectPlanRepository;

    @BeforeEach
    public void setup() {
        System.out.println("=".repeat(80));
    }

    @Order(1)
    @Test
    public void testFindAll() {
        List<ProjectPlan> projectPlans = projectPlanRepository.findAll();
        projectPlans.forEach(System.out::println);
    }



    @Order(3)
    @Test
    public void testCreateAndSaveProjectPlan() {
        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setProjectId(1);
        projectPlan.setTitle("새로운 ETC");
        projectPlan.setFilePath("path/to/file");
        projectPlan.setSampleUrl("http://example.com");
        projectPlan.setSampleImg("http://example.com/image.jpg");

        ProjectPlan savedProjectPlan = projectPlanRepository.save(projectPlan);
        Assertions.assertNotNull(savedProjectPlan.getId());
        System.out.println(savedProjectPlan);
    }

}

