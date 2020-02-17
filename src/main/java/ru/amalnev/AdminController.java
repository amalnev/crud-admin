package ru.amalnev;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequestMapping("/admax")
public class AdminController {

    @Setter(onMethod = @__(@Autowired))
    private SpringDataRepositoryScanner repositoryScanner;


    @GetMapping
    public String showEntityClassList(Model model) {

        model.addAttribute("entityClassNames", repositoryScanner.getEntityClassNames());
        return "entity-class-list";
    }

    @GetMapping("/entity-instance-list")
    public String showEntityInstanceList(Model model,
                                         @RequestParam String entityClassName) {
        CrudRepository repository = repositoryScanner.getRepository(entityClassName);
        if (repository != null) {
            List<EntityInstanceReflection> entityInstanceReflections = EntityInstanceReflection.getAllInstances(repository);
            model.addAttribute("entityInstances", entityInstanceReflections);
        }

        return "entity-instance-list";
    }
}
