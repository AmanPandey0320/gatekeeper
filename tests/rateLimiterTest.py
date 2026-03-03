import asyncio
import time
import aiohttp
import argparse

def parse_args():
    parser = argparse.ArgumentParser(description="Rate Limiter Load Tester")
    parser.add_argument("--url",     type=str,   default="http://localhost:8085/albums/1", help="Target URL")
    parser.add_argument("--reqs",    type=int,   default=20,   help="Requests per loop")
    parser.add_argument("--loops",   type=int,   default=5,    help="Number of loops")
    parser.add_argument("--delay",   type=float, default=1.0,  help="Delay between loops (seconds)")
    return parser.parse_args()


async def fire_request(session, i):
    try:
        async with session.get(TARGET_URL) as resp:
            await resp.read()
            if resp.status >= 400:
                return False, resp.status
            return True, resp.status
    except Exception as e:
        return False, -1


async def run_loop(session, loop_num, total_requests):
    start = time.time()
    tasks = [
        asyncio.create_task(fire_request(session, i))
        for i in range(total_requests)
    ]
    results = await asyncio.gather(*tasks)
    duration = time.time() - start

    success = sum(1 for ok, _ in results if ok)
    failed = total_requests - success

    status_counts = {}
    for _, status in results:
        status_counts[status] = status_counts.get(status, 0) + 1

    status_str = " | ".join(f"HTTP {s}: {c}" for s, c in sorted(status_counts.items()))
    print(f"[Loop {loop_num}] success: {success} | failed: {failed} | {duration:.3f}s | {total_requests / duration:.1f} req/s | {status_str}")


async def main():
    args = parse_args()

    # Make accessible to fire_request
    global TARGET_URL
    TARGET_URL = args.url

    connector = aiohttp.TCPConnector(
        limit=0,
        ttl_dns_cache=300,
        force_close=False,
        enable_cleanup_closed=True,
    )
    timeout = aiohttp.ClientTimeout(total=30)

    print(f"Target  : {args.url}")
    print(f"Loops   : {args.loops}  |  Reqs/loop: {args.reqs}  |  Delay: {args.delay}s")
    print("=" * 55)

    async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
        total_start = time.time()

        for loop_num in range(1, args.loops + 1):
            await run_loop(session, loop_num, args.reqs)

            if loop_num < args.loops and args.delay > 0:
                print(f"  ... waiting {args.delay}s before next loop")
                await asyncio.sleep(args.delay)  # ✅ non-blocking async sleep

        total_duration = time.time() - total_start
        print(f"\n{'=' * 55}")
        print(f"Total requests : {args.reqs * args.loops}")
        print(f"Total duration : {total_duration:.3f}s")
        print(f"Overall RPS    : {(args.reqs * args.loops) / total_duration:.1f} req/s")


if __name__ == "__main__":
    asyncio.run(main())
