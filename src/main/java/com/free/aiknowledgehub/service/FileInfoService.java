package com.free.aiknowledgehub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.free.aiknowledgehub.entity.FileInfoEntity;
import com.free.aiknowledgehub.mapper.FileInfoMapper;
import com.free.aiknowledgehub.service.impl.FileInfoServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author: Liberty-Swine
 * @Date 2026/4/7 16:09
 */
@Service
public class FileInfoService extends ServiceImpl<FileInfoMapper,FileInfoEntity> implements FileInfoServiceImpl {


}
