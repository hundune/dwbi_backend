package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.bizmq.common.MqMessageProducer;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.*;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.txt.*;
import com.yupi.springbootinit.model.entity.TextRecord;
import com.yupi.springbootinit.model.entity.TextTask;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.AiResponse;
import com.yupi.springbootinit.model.vo.TextTaskVO;
import com.yupi.springbootinit.service.TextRecordService;
import com.yupi.springbootinit.service.TextTaskService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;
import com.yupi.springbootinit.utils.TextSplitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 笔记转换接口
 *
 */
@RestController
@RequestMapping("/text")
@Slf4j
public class TextController {

    @Resource
    private TextTaskService textTaskService;

    @Resource
    private TextRecordService textRecordService;
    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private AiManager aiManager;
//    @Resource
//    private CreditService creditService;
    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private MqMessageProducer mqMessageProducer;
    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param textTaskAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTextTask(@RequestBody TextAddRequest textTaskAddRequest, HttpServletRequest request) {
        if (textTaskAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = new TextTask();
        BeanUtils.copyProperties(textTaskAddRequest, textTask);

        User loginUser = userService.getLoginUser(request);
        textTask.setUserId(loginUser.getId());
        boolean result = textTaskService.save(textTask);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newTextTaskId = textTask.getId();
        return ResultUtils.success(newTextTaskId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTextTask(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        TextTask oldTextTask = textTaskService.getById(id);
        ThrowUtils.throwIf(oldTextTask == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldTextTask.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = textTaskService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param textTaskUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateTextTask(@RequestBody TextUpdateRequest textTaskUpdateRequest) {
        if (textTaskUpdateRequest == null || textTaskUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = new TextTask();
        BeanUtils.copyProperties(textTaskUpdateRequest, textTask);
        long id = textTaskUpdateRequest.getId();
        // 判断是否存在
        TextTask oldTextTask = textTaskService.getById(id);
        ThrowUtils.throwIf(oldTextTask == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = textTaskService.updateById(textTask);
        return ResultUtils.success(result);
    }

    /**
     * 更新自己文本
     *
     * @param textTaskUpdateRequest
     * @return
     */
    @PostMapping("/my/update")
    public BaseResponse<Boolean> updateMyTextTask(@RequestBody TextUpdateRequest textTaskUpdateRequest,HttpServletRequest request) {
        if (textTaskUpdateRequest == null || textTaskUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        TextTask textTask = new TextTask();
        BeanUtils.copyProperties(textTaskUpdateRequest, textTask);
        long id = textTaskUpdateRequest.getId();
        // 判断是否存在
        TextTask oldTextTask = textTaskService.getById(id);
        ThrowUtils.throwIf(oldTextTask == null, ErrorCode.NOT_FOUND_ERROR);

        //判断为自己的文本
        ThrowUtils.throwIf(!loginUser.getId().equals(oldTextTask.getUserId()),ErrorCode.OPERATION_ERROR);
        boolean result = textTaskService.updateById(textTask);
        return ResultUtils.success(result);
    }
    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TextTask> getTextTaskById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = textTaskService.getById(id);
        if (textTask == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(textTask);
    }
    /**
     * 根据 id 获取 图表脱敏
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<TextTaskVO> getTextTaskVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = textTaskService.getById(id);
        if (textTask == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        TextTaskVO textTaskVO = new TextTaskVO();
        BeanUtils.copyProperties(textTask,textTaskVO);
        System.out.println(textTaskVO.getGenTextContent());
        return ResultUtils.success(textTaskVO);
    }
    /**
     * 分页获取列表（封装类）
     *
     * @param textTaskQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<TextTask>> listTextTaskByPage(@RequestBody TextTaskQueryRequest textTaskQueryRequest,
            HttpServletRequest request) {
        long current = textTaskQueryRequest.getCurrent();
        long size = textTaskQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<TextTask> textTaskPage = textTaskService.page(new Page<>(current, size),
                getQueryWrapper(textTaskQueryRequest));
        return ResultUtils.success(textTaskPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param textTaskQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<TextTask>> listMyTextTaskByPage(@RequestBody TextTaskQueryRequest textTaskQueryRequest,
            HttpServletRequest request) {
        if (textTaskQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        textTaskQueryRequest.setUserId(loginUser.getId());
        long current = textTaskQueryRequest.getCurrent();
        long size = textTaskQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<TextTask> textTaskPage = textTaskService.page(new Page<>(current, size),
                getQueryWrapper(textTaskQueryRequest));
        return ResultUtils.success(textTaskPage);
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param textTaskEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editTextTask(@RequestBody TextEditRequest textTaskEditRequest, HttpServletRequest request) {
        if (textTaskEditRequest == null || textTaskEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = new TextTask();
        BeanUtils.copyProperties(textTaskEditRequest, textTask);
        User loginUser = userService.getLoginUser(request);
        long id = textTaskEditRequest.getId();
        // 判断是否存在
        TextTask oldTextTask = textTaskService.getById(id);
        ThrowUtils.throwIf(oldTextTask == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldTextTask.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = textTaskService.updateById(textTask);
        return ResultUtils.success(result);
    }
    private QueryWrapper<TextTask> getQueryWrapper(TextTaskQueryRequest textTaskQueryRequest) {
        QueryWrapper<TextTask> queryWrapper = new QueryWrapper<>();

        if (textTaskQueryRequest == null) {
            return queryWrapper;
        }

        String textType = textTaskQueryRequest.getTextType();
        String name = textTaskQueryRequest.getName();
        String sortField = textTaskQueryRequest.getSortField();
        String sortOrder = textTaskQueryRequest.getSortOrder();
        Long id = textTaskQueryRequest.getId();
        Long userId = textTaskQueryRequest.getUserId();


        queryWrapper.eq(id!=null &&id>0,"id",id);
        queryWrapper.like(StringUtils.isNotEmpty(name),"name",name);
        queryWrapper.eq(StringUtils.isNoneBlank(textType),"textType",textType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
    /**
     * 文本数据上传(同步)
     *
     * @param multipartFile
     * @param genTextTaskByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<AiResponse> genTextTaskAi(@RequestPart("file") MultipartFile multipartFile,
                                                  GenTextTaskByAiRequest genTextTaskByAiRequest, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        //限流
        redisLimiterManager.doRateLimiter("doRateLimit_" + loginUser.getId());
        //获取文本任务并校验
        TextTask textTask = textTaskService.getTextTask(multipartFile, genTextTaskByAiRequest, loginUser);

        //获取任务id
        Long taskId = textTask.getId();

        String textType = textTask.getTextType();
        //从根据任务id记录表中获取数据
        QueryWrapper<TextRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("textTaskId",taskId);
        List<TextRecord> textRecords = textRecordService.list(queryWrapper);

        //将文本依次交给ai处理
        for (TextRecord textRecord : textRecords) {
            String result = null;
            result = aiManager.doChangeType(textType,textRecordService.buildUserInput(textRecord).toString());
            textRecord.setGenTextContent(result);
            textRecord.setStatus(TextConstant.SUCCEED);
            boolean updateById = textRecordService.updateById(textRecord);
            if (!updateById){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai返回结果保存失败");
            }
        }


        //将记录表中已经生成好的内容合并存入任务表
        List<TextRecord> textRecord = textRecordService.list(queryWrapper);
        StringBuilder stringBuilder = new StringBuilder();
        textRecord.forEach(textRecord1 -> {
            stringBuilder.append(textRecord1.getGenTextContent()).append('\n');
        });
        TextTask textTask1 = new TextTask();
        textTask1.setId(taskId);
        textTask1.setGenTextContent(stringBuilder.toString());
        textTask1.setStatus(TextConstant.SUCCEED);
        boolean save = textTaskService.updateById(textTask1);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"ai返回文本任务保存失败");
        AiResponse aiResponse = new AiResponse();
        aiResponse.setResultId(textTask.getId());
        return ResultUtils.success(aiResponse);

    }

    /**
     * 文本数据上传(mq)
     *
     * @param multipartFile
     * @param genTextTaskByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<AiResponse> genTextTaskAsyncAiMq(@RequestPart("file") MultipartFile multipartFile,
                                                              GenTextTaskByAiRequest genTextTaskByAiRequest, HttpServletRequest request) {


        User loginUser = userService.getLoginUser(request);
        //限流
        redisLimiterManager.doRateLimiter("doRateLimit_" + loginUser.getId());
        //获取文本任务并校验
        TextTask textTask = textTaskService.getTextTask(multipartFile, genTextTaskByAiRequest, loginUser);

        //获取任务id
        Long taskId = textTask.getId();

        log.warn("准备发送信息给队列，Message={}=======================================",taskId);
        mqMessageProducer.sendMessage(MqConstant.TEXT_EXCHANGE_NAME,MqConstant.TEXT_ROUTING_KEY,String.valueOf(taskId));
        //返回数据参数
        AiResponse aiResponse = new AiResponse();
        aiResponse.setResultId(textTask.getId());
        return ResultUtils.success(aiResponse);

    }

    /**
     * 文本重新生成(mq)
     *
     * @param textRebuildRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/rebuild")
    public BaseResponse<AiResponse> genTextTaskAsyncAiRebuild(TextRebuildRequest textRebuildRequest, HttpServletRequest request) {
        Long textTaskId = textRebuildRequest.getId();
        //获取记录表
        List<TextRecord> recordList = textRecordService.list(new QueryWrapper<TextRecord>().eq("textTaskId", textTaskId));
        //校验，查看原始文本是否为空
        recordList.forEach(textRecord -> {
            ThrowUtils.throwIf(StringUtils.isBlank(textRecord.getTextContent()),ErrorCode.PARAMS_ERROR,"文本为空");
        });

        User loginUser = userService.getLoginUser(request);
        //限流
        redisLimiterManager.doRateLimiter("doRateLimit_" + loginUser.getId());

        //保存数据库 wait
        TextTask textTask = new TextTask();
        textTask.setStatus(TextConstant.WAIT);
        textTask.setId(textTaskId);
        boolean saveResult = textTaskService.updateById(textTask);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"文本保存失败");
        log.warn("准备发送信息给队列，Message={}=======================================",textTaskId);
        mqMessageProducer.sendMessage(MqConstant.TEXT_EXCHANGE_NAME,MqConstant.TEXT_ROUTING_KEY,String.valueOf(textTaskId));
        //返回数据参数
        AiResponse aiResponse = new AiResponse();
        aiResponse.setResultId(textTask.getId());
        return ResultUtils.success(aiResponse);

    }
}