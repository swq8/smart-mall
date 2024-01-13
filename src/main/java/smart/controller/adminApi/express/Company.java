package smart.controller.adminApi.express;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.entity.ExpressCompanyEntity;
import smart.lib.ApiJsonResult;
import smart.repository.ExpressCompanyRepository;
import smart.service.AdminLogService;
import smart.service.ExpressCompanyService;
import smart.dto.IdDto;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;


@RestController(value = "adminApi/express/company")
@RequestMapping(path = "/adminApi/express/company", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Company {
    @Resource
    AdminLogService adminLogService;
    @Resource
    ExpressCompanyRepository expressCompanyRepository;

    @Resource
    ExpressCompanyService expressCompanyService;

    @Authorize("/express/company/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class) ExpressCompanyEntity entity, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(expressCompanyService.save(entity));
        adminLogService.addLogIfSuccess(result, request, "新建快递公司", entity);
        return result;
    }

    @Authorize("/express/company/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(expressCompanyService.delete(idDto.getId()));
        adminLogService.addLogIfSuccess(result, request, "删除快递公司", idDto);
        return result;
    }

    @Authorize("/express/company/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class) ExpressCompanyEntity entity, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(expressCompanyService.save(entity));
        adminLogService.addLogIfSuccess(result, request, "编辑快递公司", entity);
        return result;
    }

    @Authorize("/express/company/query")
    @PostMapping("list")
    public ApiJsonResult list() {
        return ApiJsonResult.success()
                .putDataItem("rows", expressCompanyRepository.findAllByOrderByOrderNum());
    }
}
