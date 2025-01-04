package com;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@MultipartConfig
public class RemoveCommentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String directoryName = request.getParameter("directoryName");
        String[] fileTypes = request.getParameterValues("fileType");
        String[] javaCommentTypes = request.getParameterValues("javaCommentType");
        String[] vbaCommentTypes = request.getParameterValues("vbaCommentType");
        
        boolean javaFileSelected = (fileTypes != null && Arrays.asList(fileTypes).contains("javaFile"));
        boolean vbaFileSelected = (fileTypes != null && Arrays.asList(fileTypes).contains("vbaFile"));
        
       
    	boolean inlineJava = (javaCommentTypes != null && Arrays.asList(javaCommentTypes).contains("inlineJava"));
    	boolean singleLineJava = (javaCommentTypes != null && Arrays.asList(javaCommentTypes).contains("singleLineJava"));
    	boolean multiLineJava = (javaCommentTypes != null && Arrays.asList(javaCommentTypes).contains("multiLineJava"));
    
  
    	boolean inlineVba = (vbaCommentTypes != null && Arrays.asList(vbaCommentTypes).contains("inlineVba"));
    	boolean singleLineVba = (vbaCommentTypes != null && Arrays.asList(vbaCommentTypes).contains("singleLineVba"));
		
        
        log(inlineJava + " " + singleLineJava + " " + multiLineJava + " " + inlineVba + " " + singleLineVba);
        
        
        Collection<Part> fileParts = request.getParts();
        
        log(fileParts.isEmpty()? "True": "False");

        String baseOutputDirectory = System.getProperty("java.io.tmpdir");
        String newDirectoryName = directoryName;
        Path outputDirectoryPath = Paths.get(baseOutputDirectory, newDirectoryName);

        Files.createDirectories(outputDirectoryPath);

        for (Part filePart : fileParts) {
        	String fileName = filePart.getSubmittedFileName();
        	if (fileName == null || fileName.isEmpty()) continue;

        	// Retrieve the relative path of the file
        	String submittedFileName = filePart.getSubmittedFileName();
        	Path relativePath = Paths.get(submittedFileName).normalize(); // Keeps folder structure relative

        	String fileNameOnly = relativePath.getFileName().toString();
        	String fileExtension = getFileExtension(fileNameOnly);
        	log(fileExtension);
        	if (!fileExtension.equals("java") && !fileExtension.equals("vba")) continue;
        	if (fileExtension.equals("java") && !javaFileSelected) continue;
        	if (fileExtension.equals("vba") && !vbaFileSelected) continue;

        	InputStream fileContent = filePart.getInputStream();
        	String fileText = readFileContent(fileContent);

        	String processedContent = null;
        	if (fileExtension.equals("java")) {
        	    processedContent = removeJavaComments(fileText, inlineJava, singleLineJava, multiLineJava);
        	} else if (fileExtension.equals("vba")) {
        	    processedContent = removeVbaComments(fileText, inlineVba, singleLineVba);
        	}

        	Path outputPath = outputDirectoryPath.resolve(relativePath);
        	Files.createDirectories(outputPath.getParent()); // Ensure parent directories exist

        	try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
        	    writer.write(processedContent);
        	}
        }

        File zipFile = new File(baseOutputDirectory, newDirectoryName + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walk(outputDirectoryPath)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            String relativePath = outputDirectoryPath.relativize(filePath).toString();
                            zos.putNextEntry(new ZipEntry(relativePath));
                            Files.copy(filePath, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFile.getName());

        try (FileInputStream fis = new FileInputStream(zipFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        Files.walk(outputDirectoryPath)
                .sorted((a, b) -> b.compareTo(a)) 
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        zipFile.delete();
    }

    private String readFileContent(InputStream fileContent) throws IOException {
        StringBuilder fileText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileText.append(line).append("\n");
            }
        }
        return fileText.toString();
    }

    private String removeJavaComments(String code, boolean inlineJava, boolean singleLineJava, boolean multiLineJava) {
        String singleLineComment = "//.*?$";
        String multiLineComment = "/\\*.*?\\*/";

        StringBuilder filteredCodeTrial = new StringBuilder();
        String[] trailLines = code.split("\n");
        
        String withoutComments = null;
        if(multiLineJava) {
        	int i = 0;
        	for (i = 0; i < trailLines.length - 1; i++) {
        		if (trailLines[i].trim().isEmpty() && trailLines[i + 1].trim().startsWith("/*")) {
        			continue;
        		}
        		filteredCodeTrial.append(trailLines[i]).append("\n");
        	}
        	filteredCodeTrial.append(trailLines[i]).append("\n");
               	
        	Pattern pattern = Pattern.compile(multiLineComment, Pattern.DOTALL | Pattern.MULTILINE);
        	Matcher matcher = pattern.matcher(filteredCodeTrial);
        	withoutComments = matcher.replaceAll("");
        }
        
        if(!multiLineJava) withoutComments = code;
        
        if (singleLineJava || inlineJava) {        	
        	Pattern singleLinePattern = Pattern.compile(singleLineComment, Pattern.MULTILINE);
        	String[] lines = withoutComments.split("\n");
        	StringBuilder filteredCode = new StringBuilder();
        	
        	for (String line : lines) {
        		String checkLine = line.trim();
        		if (checkLine.startsWith("//") && singleLineJava) {
        			continue;
        		}
        		if(checkLine.startsWith("//") && !singleLineJava) {
        			filteredCode.append(line).append("\n");
        			continue;
        		}
        		if (singleLinePattern.matcher(line).find() && inlineJava) {
        			line = singleLinePattern.matcher(line).replaceAll("");
        		}
        		filteredCode.append(line).append("\n");
        	}
        	return filteredCode.toString();
        }
        return withoutComments;
    }

    private String removeVbaComments(String code, Boolean inlineVba, Boolean singleLineVba) {
        String vbaComment = "'[^\n]*";
        String[] newCode = code.split("\n");
        StringBuilder filteredCode = new StringBuilder();

        Pattern pattern = Pattern.compile(vbaComment, Pattern.MULTILINE);
 
        for (int i = 0; i < newCode.length; i++) {
            if (newCode[i].trim().startsWith("'") && singleLineVba) {
                continue;
            }
            if (newCode[i].trim().startsWith("'") && !singleLineVba) {
				filteredCode.append(newCode[i]).append("\n");
				continue;
			}
            if(inlineVba && pattern.matcher(newCode[i]).find()) {
            	String line = pattern.matcher(newCode[i]).replaceAll("");
            	filteredCode.append(line).append("\n");
            	continue;
            }
            
            filteredCode.append(newCode[i]).append("\n");
        }

        return filteredCode.toString();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
