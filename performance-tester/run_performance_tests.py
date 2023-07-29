import json
import os
from statistics import median
import subprocess
import sys
import math
from dataclasses import dataclass
from datetime import datetime

import plotly.graph_objects as go


@dataclass
class ApiBenchmarkResult:
    request_count: int
    total_duration_ms: int
    request_duration_nanos: list[int]
    request_duration_ms: list[float]

    def __init__(
        self,
        request_count: int,
        total_duration_ms: int,
        request_duration_nanos: list[int],
    ):
        self.request_count = request_count
        self.total_duration_ms = total_duration_ms
        self.request_duration_nanos = request_duration_nanos
        self.request_duration_ms = [num / 1_000_000 for num in request_duration_nanos]


@dataclass
class BenchmarkResult:
    file_name: str
    description: str
    concurrent_users: int
    grpc_api_benchmark: ApiBenchmarkResult
    rest_api_benchmark: ApiBenchmarkResult


def load_all_benchmark_results(directory: str) -> list[BenchmarkResult]:
    files = [
        f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))
    ]

    return [load_benchmark_result(directory, file) for file in files]


def load_benchmark_result(directory: str, file_name: str) -> BenchmarkResult:
    with open(f"{directory}/{file_name}", "r") as file:
        json_data = json.load(file)

    sanitized_file_name = file_name.split(".json")[0]

    grpc_benchmark_json = json_data["grpcApiBenchmark"]
    rest_benchmark_json = json_data["restApiBenchmark"]

    grpc_api_benchmark = ApiBenchmarkResult(
        grpc_benchmark_json["requestCount"],
        grpc_benchmark_json["totalDurationMs"],
        grpc_benchmark_json["requestDurationNanos"],
    )
    rest_api_benchmark = ApiBenchmarkResult(
        rest_benchmark_json["requestCount"],
        rest_benchmark_json["totalDurationMs"],
        rest_benchmark_json["requestDurationNanos"],
    )

    return BenchmarkResult(
        sanitized_file_name,
        json_data["description"],
        json_data["concurrentUsers"],
        grpc_api_benchmark,
        rest_api_benchmark,
    )


def visualize_request_duration(benchmark: BenchmarkResult, output_path: str):
    fig = go.Figure(
        layout={
            # "title": benchmark.description,
            "xaxis_title": "API-Type",
            "yaxis_title": "Request duration (ms)",
        }
    )
    fig.add_trace(
        go.Box(y=benchmark.grpc_api_benchmark.request_duration_ms, name="gRPC API")
    )
    fig.add_trace(
        go.Box(
            y=benchmark.rest_api_benchmark.request_duration_ms,
            name="REST API",
            marker_color="lightseagreen",
        )
    )

    # with logarithmic scale
    fig.update_layout(showlegend=False, margin=dict(l=5,r=5,b=5,t=5), yaxis_type="log")
    # without logarithmic scale
    # fig.update_layout(showlegend=False, margin=dict(l=5, r=5, b=5, t=5))
    fig.write_image(f"{output_path}/{benchmark.file_name}_req_duration.png")


def visualize_requests_per_second(benchmark: BenchmarkResult, output_path: str):
    grpc_requests_per_sec = round(
        benchmark.grpc_api_benchmark.request_count
        / (benchmark.grpc_api_benchmark.total_duration_ms / 1000)
    )
    rest_requests_per_sec = round(
        benchmark.rest_api_benchmark.request_count
        / (benchmark.rest_api_benchmark.total_duration_ms / 1000)
    )

    x_axis = ["gRPC API", "REST API"]
    y_axis = [grpc_requests_per_sec, rest_requests_per_sec]

    fig = go.Figure()
    fig.add_bar(
        x=x_axis,
        y=y_axis,
        text=y_axis,
        textposition="auto",
        marker={"color": ["#1f77b4", "lightseagreen"]},
    )
    fig.update_layout(
        xaxis_title="API-Type",
        yaxis_title="Requests per second",
        title_font=dict(
            size=14,
        ),
        margin=dict(l=5, r=5, b=5, t=5),
    )

    fig.write_image(f"{output_path}/{benchmark.file_name}_req_per_sec.png")


def calculate_median(benchmark: BenchmarkResult, file_path: str):
    median_grpc = round(median(benchmark.grpc_api_benchmark.request_duration_ms), 3)
    median_rest = round(median(benchmark.rest_api_benchmark.request_duration_ms), 3)

    with open(f"{file_path}/{benchmark.file_name}.txt", "w") as file:
        file.write(f"Description: {benchmark.description}\n")
        file.write(f"gRPC request duration median: {median_grpc}ms\n")
        file.write(f"REST request duration median: {median_rest}ms\n")

        if median_grpc < median_rest:
            factor = round(median_rest / median_grpc, 2)
            faster = "gRPC"
            slower = "REST"
        else:
            factor = round(median_grpc / median_rest, 2)
            faster = "REST"
            slower = "gRPC"

        file.write(f"{faster} requests were {factor} times faster than {slower}")


def visualize_multiple_req_per_sec():
    data_file_path = f"performance-tester/performance_test_data/multiple_req_per_sec"
    response_sizes = ["10 KB", "1 MB", "10 MB"]

    benchmarks = load_all_benchmark_results(data_file_path)

    all_grpc_req_per_sec = []
    all_rest_req_per_sec = []

    for benchmark in benchmarks:
        grpc_requests_per_sec = round(
            benchmark.grpc_api_benchmark.request_count
            / (benchmark.grpc_api_benchmark.total_duration_ms / 1000)
        )
        rest_requests_per_sec = round(
            benchmark.rest_api_benchmark.request_count
            / (benchmark.rest_api_benchmark.total_duration_ms / 1000)
        )

        all_grpc_req_per_sec.append(grpc_requests_per_sec)
        all_rest_req_per_sec.append(rest_requests_per_sec)

    fig = go.Figure()
    fig.add_trace(
        go.Bar(
            x=response_sizes,
            y=all_grpc_req_per_sec,
            name="gRPC API",
            marker_color="#1f77b4",
            textposition="auto",
            text=all_grpc_req_per_sec,
        )
    )
    fig.add_trace(
        go.Bar(
            x=response_sizes,
            y=all_rest_req_per_sec,
            name="REST API",
            marker_color="lightseagreen",
            textposition="auto",
            text=all_rest_req_per_sec,
        )
    )

    fig.update_layout(
        barmode="group",
        yaxis_title="Requests per second",
        xaxis_title="Response data size",
        margin=dict(l=5, r=5, b=5, t=5),
    )
    fig.write_image(
        "performance-tester/plots/custom_test/throughput_different_response_sizes.png"
    )


def get_timestamp() -> str:
    return datetime.today().strftime("%Y-%m-%d-%H-%M-%S")
    # return "2023-05-29-16-51-57"


def start_performance_test_application(output_file_path: str) -> int:
    print(f"Starting the performance-tester with output_file_path: {output_file_path}")
    popen = subprocess.Popen(
        [
            "java",
            "-jar",
            "performance-tester/target/performance-tester-1.0.0-shaded.jar",
            output_file_path,
        ],
        stdout=subprocess.PIPE,
        universal_newlines=True,
    )
    print("Output of the performance-tester:")
    for line in popen.stdout:
        print(line, end="")
    popen.stdout.close()
    return_code = popen.wait()
    print(f"Performance-tester finished with code: {return_code}")
    return return_code


if __name__ == "__main__":
    visualize_only = False
    args = sys.argv[1:]
    if len(args) == 2 and args[0] == "--vis":
        visualize_only = True
    elif len(args) == 1 and args[0] == "--custom":
        visualize_multiple_req_per_sec()
        exit(0)

    if visualize_only:
        output_directory = args[1]
    else:
        output_directory = get_timestamp()

    performance_data_file_path = (
        f"performance-tester/performance_test_data/{output_directory}"
    )
    plots_file_path = f"performance-tester/plots/{output_directory}"

    if not visualize_only:
        return_code = start_performance_test_application(performance_data_file_path)
        if return_code != 0:
            exit(1)

    benchmark_results = load_all_benchmark_results(performance_data_file_path)

    if not os.path.exists(plots_file_path):
        os.mkdir(plots_file_path)

    for i, benchmark_result in enumerate(benchmark_results):
        visualize_request_duration(benchmark_result, plots_file_path)
        visualize_requests_per_second(benchmark_result, plots_file_path)
        calculate_median(benchmark_result, plots_file_path)

    print("Successfully created all plots")
