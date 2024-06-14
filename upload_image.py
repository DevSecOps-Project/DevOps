import sys

import utils.aws_util
import utils.constants
import utils.docker_util


if __name__ == "__main__":
    try:
        docker_tag = utils.aws_util.get_latest_image_version()
        new_docker_tag = utils.docker_util.increment_tag(docker_tag)
        tagged_image = utils.docker_util.tag_docker_image(new_docker_tag)
        utils.aws_util.ecr_authenticate()
        utils.docker_util.push_docker_image_to_ecr(tagged_image)
        new_tag = utils.aws_util.strip_version_val(new_docker_tag)
        if utils.aws_util.is_last_version(new_tag):
            print("Docker image uploaded successfully to ECR")
        else:
            print("Docker image upload to ECR failed")
            raise ValueError(f'image {new_docker_tag} was not found on aws')
    except Exception as e:
        print(f"Error occurred while uploading Docker image to ECR: {e}")
        sys.exit(1)
