import asyncio
import time
import aiohttp

TARGET_URL = "http://localhost:8085/todos/1"
TOTAL_REQUESTS = 1000
LOOPS = 5

async def fire_request(session, i):
    try:
        async with session.get(TARGET_URL) as resp:
            await resp.read()
            return True
    except Exception as e:
        return False

async def run_loop(session, loop_num):
    start = time.time()
    tasks = [
        asyncio.create_task(fire_request(session, i))
        for i in range(TOTAL_REQUESTS)
    ]
    results = await asyncio.gather(*tasks)
    duration = time.time() - start

    success = sum(results)
    failed = TOTAL_REQUESTS - success
    print(f"[Loop {loop_num}] ✅ {success} | ❌ {failed} | ⏱ {duration:.3f}s | 🚀 {TOTAL_REQUESTS / duration:.1f} req/s")

async def main():
    connector = aiohttp.TCPConnector(
        limit=0,
        ttl_dns_cache=300,
        force_close=False,
        enable_cleanup_closed=True,
    )
    timeout = aiohttp.ClientTimeout(total=30)

    async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
        total_start = time.time()

        for loop_num in range(1, LOOPS + 1):
            await run_loop(session, loop_num)

        total_duration = time.time() - total_start
        print(f"\n{'='*50}")
        print(f"Total requests : {TOTAL_REQUESTS * LOOPS}")
        print(f"Total duration : {total_duration:.3f}s")
        print(f"Overall RPS    : {(TOTAL_REQUESTS * LOOPS) / total_duration:.1f} req/s")

if __name__ == "__main__":
    asyncio.run(main())
