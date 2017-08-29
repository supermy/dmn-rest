package logviewer.web;

/**
 * Created by moyong on 16/10/28.
 */


import logviewer.domain.FileNode;
import logviewer.service.LogViewerService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/logviewer")
public class LogViewerController {

    protected final Logger log = LogManager.getLogger(getClass());
    @Autowired
    private LogViewerService logViewerService;

    /**
     * Build list of all files in the log directory
     *
     * @return JSON response with objects expected by a tree grid.
     */
    @RequestMapping(value = "/files")
    public
    @ResponseBody
    Object getFiles(HttpServletResponse response) {
        try {
            List<FileNode> files = logViewerService.getFiles();
            return files;
        } catch (Exception e) {
            log.error("Error loading files ", e);
            // This just returns a Map that will result in JSON {"success":"false", "message":"Unexpected Error"}
//            Map<String, Object> errorMap = WebUtil.getModelMapMessage(false, ControllerConstants.DEFAULT_ERROR_MSG, null);
            Map<String, Object> errorMap = new HashMap();
            errorMap.put("error", e.getMessage());
            return errorMap;
        }
    }

    /**
     * Open a specified file in a browser window/tab.  Some of the success of the
     * open might depend on client-side settings.
     *
     * @param request
     * @param response
     * @param fileName The name of the file to open, including path info if not in
     *                 the root log directory.
     */
    @RequestMapping(value = "/open")
    public void openFile(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam("fileName") String fileName) {
        try {

            File file = logViewerService.getFile(fileName);


            byte[] content = new byte[(int) file.length()];
            response.setContentType("text/plain");
            response.addHeader("content-disposition", "inline;filename=" + fileName);
            response.setContentLength(content.length);
            FileInputStream in = new FileInputStream(file);
            FileCopyUtils.copy(in, response.getOutputStream());
        } catch (Exception e) {
            log.error("Something happened", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Download the selected file to the client.
     *
     * @param request
     * @param response
     * @param fileName The name of the file to open, including path info if not in
     *                 the root log directory.
     */
    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public void downloadFile(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam("fileName") String fileName) {
        try {

            System.out.println("============");
            System.out.println(fileName);
            System.out.println("============");
            String[] name=fileName.split(",");


            File file = logViewerService.getFile(name[0].replace("\"",""));

            log.debug(file.getName());
            log.debug(file.exists());

            byte[] content = new byte[(int) file.length()];
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("Cache-Control", "cache, must-revalidate");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength(content.length);
            FileInputStream in = new FileInputStream(file);
            FileCopyUtils.copy(in, response.getOutputStream());

        } catch (Exception e) {
            log.error("Something happened", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 单个文件上传，测试通过
     * @param file
     * @param desc
     * @return
     */
    @RequestMapping(value="/upload", method=RequestMethod.POST )
    public @ResponseBody
    Map singleSave(@RequestParam("upload") MultipartFile file){
//        System.out.println("File Description:"+desc);
        Map result = new HashMap();

        if (!file.isEmpty()) {
//            try {

            log.debug(file.getOriginalFilename());
            System.out.println(file.getContentType());
            System.out.println(file.getName());
            System.out.println(file.getOriginalFilename());

            boolean save = logViewerService.save(file);


//
//                String filename = UUID.randomUUID().toString()+"."+FileUtils.getExtensionName(file.getOriginalFilename());
//
//                Path target = Paths.get(ROOT, filename);
//                Files.copy(file.getInputStream(), target);
//
//                log.debug(target.getFileName().toString());
//                log.debug(target.getParent().getFileName().toString());
//                log.debug(target.toString());
//                log.debug(target.toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
//
//                log.debug("=============ContentType:"+file.getContentType());
////                FilenameUtils.getExtension()
//                log.debug(Files.probeContentType(target));
//
//
//                Avatar img=new Avatar();
//                img.setFilename(file.getOriginalFilename());
//                img.setFilesize(file.getSize());
//                img.setWebpath(ROOT+"/"+filename);
//                img.setSyspath(target.toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
//                img.setCreateDate(new Date());
//                img.setCreateBy("user");
//                avatarRepository.save(img);
//
//
//                result.put("data","");
//                result.put("upload",img);
//                Map m = new HashMap();
//
//                List<Avatar> files = avatarRepository.findAll();
//                Map mm=new HashMap();
//                for (Avatar f:files
//                        ) {
//                    mm.put(f.getId(),f);
//                }
//                m.put("files", mm);
//
//                result.put("files",m);
////                result.put("files",avatarRepository.findAll());
//
            result.put("desc","文件上传成功"+file.getOriginalFilename());
            result.put("success",save);

                return result;
//
//            } catch (IOException e) {
//                result.put("desc","文件上传失败"+file.getOriginalFilename());
//                return result;
//            }
        } else {
            result.put("desc", "Failed to upload " + file.getOriginalFilename() + " because it was empty");
            return result;
        }

    }
}
