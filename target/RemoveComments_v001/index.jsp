<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Remove Comments</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome for Icons -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa; /* Light background */
            font-family: 'Arial', sans-serif;
        }
        .container {
            max-width: 600px;
            margin-top: 50px;
            padding: 30px;
            background-color: #ffffff; /* White card-like background */
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
        .main-title {
            text-align: center;
            margin-bottom: 30px;
            color: #007bff;
        }
        .btn-submit {
            background-color: #007bff;
            color: #ffffff;
        }
        .btn-submit:hover {
            background-color: #0056b3;
            color: #ffffff;
        }
        .file-input-label {
            margin-bottom: 15px;
            font-weight: bold;
            color: #333333;
        }
        .info-text {
            margin-top: 10px;
            font-size: 0.9rem;
            color: #6c757d;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1 class="main-title">
            <i class="fas fa-code"></i> Remove Comments from Java and VBA Files
        </h1>
        <form action="RemoveCommentsServlet" method="post" enctype="multipart/form-data">
            <div class="mb-3">
                <label for="fileInput" class="form-label file-input-label">
                    <i class="fas fa-folder-open"></i> Select a Folder:
                </label>
                <input type="file" id="fileInput" name="file" class="form-control" webkitdirectory directory multiple onchange="handleDirectorySelection(event)" required>
                <input type="hidden" id="directoryName" name="directoryName">
                <div class="info-text">
                    Please select a folder containing Java or VBA files. All files in the folder will be processed, and comments will be removed.
                </div>
            </div>
            <button type="submit" class="btn btn-submit btn-lg w-100">
                <i class="fas fa-play-circle"></i> Process Files
            </button>
        </form>
    </div>

    <!-- Bootstrap JS and Dependencies -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function handleDirectorySelection(event) {
            const files = event.target.files;
            if (files.length > 0) {
                const fullPath = files[0].webkitRelativePath;
                const directoryName = fullPath.split('/')[0];

                document.getElementById('directoryName').value = directoryName;
            }
        }
    </script>
</body>
</html>
