# Gatekeeper Load Tester

A lightweight async HTTP load testing script to validate rate limiter behavior.
Built with `aiohttp` and `asyncio` for high-concurrency burst testing.

---

## Requirements

- Python 3.8+
- `aiohttp`

Install dependencies:
```bash
pip install aiohttp
```
## Usage

```bash
python test.py [OPTIONS]
```
| Flag    | Type  | Default                        | Description                            |
| ------- | ----- | ------------------------------ | -------------------------------------- |
| --url   | str   | http://localhost:8085/albums/1 | Target endpoint URL                    |
| --reqs  | int   | 20                             | Number of concurrent requests per loop |
| --loops | int   | 5                              | Number of test loops to run            |
| --delay | float | 1.0                            | Delay in seconds between loops         |