package izhar.personal.com.multi_threaded_file_processing_system.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usage")
public class ResourceUsageController {

  @GetMapping("/getResourceUsage")
  public ResponseEntity<Map<String, Object>> getResourceUsage() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    double processCpuLoad = osBean.getProcessCpuLoad() * 100;
    double systemCpuLoad = osBean.getSystemCpuLoad() * 100;
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("memoryUsage in MB's", usedMemory);
    response.put("ProcessCpuLoad", processCpuLoad);
    response.put("SystemCpuLoad", systemCpuLoad);
    return ResponseEntity.ok(response);
  }
}
