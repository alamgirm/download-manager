# Download Manager

A JDownloader-like command line download manager with async queue processing and parallel downloads. This tool accepts URLs as command line arguments and processes them asynchronously in the background.

## Features

- **Command Line Interface**: Simple URL-based command line interface
- **Async Queue Processing**: Downloads are queued and processed asynchronously
- **Parallel Downloads**: Multiple downloads can run simultaneously
- **Progress Tracking**: Real-time download progress with speed and percentage
- **Resume Support**: Automatic filename conflict resolution
- **Error Handling**: Robust error handling with retry logic
- **Status Tracking**: Comprehensive download status tracking

## Prerequisites

- Java 21 or higher
- Gradle 8.5 or higher

## Quick Start

### 1. Build the Application

```bash
./gradlew build
```

### 2. Run the Download Manager

```bash
# Download a single file
java -jar build/libs/download-manager-1.0.0.jar https://example.com/file.zip

# Download multiple files
java -jar build/libs/download-manager-1.0.0.jar \
  https://example.com/file1.zip \
  https://example.com/file2.pdf \
  https://example.com/file3.mp4
```

## Usage Examples

### Basic Usage

```bash
# Download a single file
java -jar download-manager.jar https://httpbin.org/bytes/1048576

# Download multiple files
java -jar download-manager.jar \
  https://httpbin.org/bytes/1024 \
  https://httpbin.org/bytes/2048 \
  https://httpbin.org/bytes/4096
```

### Real-world Examples

```bash
# Download software packages
java -jar download-manager.jar \
  https://download.docker.com/linux/static/stable/x86_64/docker-24.0.7.tgz \
  https://downloads.mongodb.org/linux/mongodb-linux-x86_64-ubuntu2004-6.0.14.tgz

# Download documentation
java -jar download-manager.jar \
  https://docs.spring.io/spring-boot/docs/3.2.0/reference/pdf/spring-boot-reference.pdf
```

## Architecture

The download manager is built with a modular architecture inspired by JDownloader:

### Core Components

1. **DownloadManagerApplication**: Main application class that accepts URLs
2. **DownloadQueueService**: Manages the async download queue
3. **DownloadService**: Handles actual HTTP downloads with progress tracking
4. **DownloadTask**: Model representing a download job with status tracking

### Download Flow

1. **URL Input**: URLs are provided as command line arguments
2. **Queue Addition**: Each URL is added to the download queue as a task
3. **Async Processing**: A background processor picks up tasks from the queue
4. **Parallel Downloads**: Multiple downloads can run simultaneously
5. **Progress Tracking**: Real-time progress updates with speed and percentage
6. **File Management**: Automatic filename conflict resolution
7. **Completion**: Downloads are saved to the `downloads/` directory

### Queue Management

- **Blocking Queue**: Uses `LinkedBlockingQueue` for thread-safe operations
- **Async Processing**: Spring's `@Async` for non-blocking download processing
- **Concurrent Downloads**: Configurable maximum concurrent downloads (default: 3)
- **Status Tracking**: Each task has comprehensive status tracking

## Configuration

### Download Settings

The following settings can be configured in `application.yml`:

```yaml
download:
  max-concurrent: 3
  directory: downloads
  timeout:
    connect: 30s
    read: 60s
    write: 60s
  buffer-size: 8192
```

### HTTP Client Settings

- **Connect Timeout**: 30 seconds
- **Read Timeout**: 60 seconds
- **Write Timeout**: 60 seconds
- **User Agent**: DownloadManager/1.0

## File Management

### Download Directory

Files are downloaded to the `downloads/` directory (created automatically).

### Filename Resolution

1. **URL Extraction**: Filename is extracted from the URL path
2. **Conflict Resolution**: If a file exists, a number is appended: `file (1).zip`
3. **Fallback**: If no filename can be extracted, a timestamp is used

### Progress Tracking

- **Real-time Updates**: Progress is updated every 8KB of data
- **Logging**: Progress is logged every 1MB with speed and percentage
- **Status Tracking**: Comprehensive status tracking (Queued, Downloading, Completed, Failed)

## Error Handling

### Network Errors

- **Connection Timeouts**: Automatic retry with exponential backoff
- **HTTP Errors**: Proper error messages with HTTP status codes
- **File System Errors**: Graceful handling of disk space and permission issues

### Recovery

- **Failed Downloads**: Failed tasks are marked with error messages
- **Partial Downloads**: Incomplete downloads are cleaned up
- **Queue Continuation**: Other downloads continue even if one fails

## Performance Features

### Parallel Processing

- **Concurrent Downloads**: Multiple downloads run simultaneously
- **Thread Pool**: Efficient thread management for async operations
- **Memory Management**: Streaming downloads to avoid memory issues

### Progress Monitoring

- **Speed Calculation**: Real-time download speed calculation
- **Progress Percentage**: Accurate progress percentage tracking
- **File Size Display**: Human-readable file size formatting

## Comparison with JDownloader

This download manager provides a simplified version of JDownloader's core functionality:

| Feature | JDownloader | This Download Manager |
|---------|-------------|----------------------|
| GUI | ✅ Full GUI | ❌ Command line only |
| Plugin System | ✅ Extensive | ❌ Basic HTTP only |
| Resume Downloads | ✅ Yes | ❌ No (planned) |
| Parallel Downloads | ✅ Yes | ✅ Yes |
| Progress Tracking | ✅ Yes | ✅ Yes |
| Queue Management | ✅ Yes | ✅ Yes |
| Error Handling | ✅ Advanced | ✅ Basic |

## Development

### Building from Source

```bash
git clone <repository-url>
cd download-manager
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Code Structure

```
src/main/java/com/downloadmanager/
├── DownloadManagerApplication.java    # Main application
├── model/
│   └── DownloadTask.java             # Download task model
└── service/
    ├── DownloadQueueService.java     # Queue management
    └── DownloadService.java          # HTTP downloads
```

## Future Enhancements

- **Resume Downloads**: Support for resuming interrupted downloads
- **Plugin System**: Extensible plugin architecture for different protocols
- **GUI Interface**: Optional web-based GUI
- **Download Scheduling**: Scheduled downloads
- **Bandwidth Limiting**: Configurable bandwidth limits
- **Proxy Support**: HTTP/HTTPS proxy configuration
- **Authentication**: Support for authenticated downloads

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

This project is inspired by [JDownloader](https://jdownloader.org/), an excellent open-source download manager. The JDownloader source code can be found at the [JDownloader mirror repository](https://github.com/mycodedoesnotcompile2/jdownloader_mirror). 